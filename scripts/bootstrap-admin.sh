#!/usr/bin/env bash
set -euo pipefail

POSTGRES_USER="quizz"
POSTGRES_DB="quizz"
ADMIN_EMAIL=""
ADMIN_FULL_NAME=""
ADMIN_PASSWORD=""
ADMIN_PASSWORD_CONFIRM=""
PASSWORD_HASH=""
PROJECT_ROOT=""

require_command() {
    local command_name="$1"

    if ! command -v "$command_name" >/dev/null 2>&1; then
        printf 'Required command not found: %s\n' "$command_name" >&2
        exit 1
    fi
}

resolve_project_root() {
    local script_dir

    script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    PROJECT_ROOT="$(cd "$script_dir/.." && pwd)"
}

read_admin_input() {
    local email_input
    local full_name_input

    read -r -p "Admin email [admin@example.com]: " email_input
    ADMIN_EMAIL="${email_input:-admin@example.com}"

    read -r -p "Admin full name [Admin User]: " full_name_input
    ADMIN_FULL_NAME="${full_name_input:-Admin User}"

    read -r -s -p "Admin password: " ADMIN_PASSWORD
    printf '\n'

    read -r -s -p "Confirm admin password: " ADMIN_PASSWORD_CONFIRM
    printf '\n'

    validate_password
}

validate_password() {
    if [[ -z "$ADMIN_PASSWORD" ]]; then
        printf 'Admin password is required.\n' >&2
        exit 1
    fi

    if (( ${#ADMIN_PASSWORD} < 12 )); then
        printf 'Admin password must be at least 12 characters long.\n' >&2
        exit 1
    fi

    if [[ "$ADMIN_PASSWORD" != "$ADMIN_PASSWORD_CONFIRM" ]]; then
        printf 'Admin password confirmation does not match.\n' >&2
        exit 1
    fi
}

ensure_secret_file_exists() {
    local secret_file="$PROJECT_ROOT/docker/secrets/postgres_password.txt"

    if [[ -f "$secret_file" ]]; then
        return
    fi

    printf 'Missing Docker secret file: docker/secrets/postgres_password.txt\n\n' >&2
    printf 'Create it manually before running this script:\n' >&2
    printf '  mkdir -p docker/secrets\n' >&2
    printf '  umask 077\n' >&2
    printf "  openssl rand -base64 48 | tr -d '\\n' > docker/secrets/postgres_password.txt\n" >&2
    printf '  chmod 600 docker/secrets/postgres_password.txt\n' >&2
    exit 1
}

ensure_compose_services() {
    (
        cd "$PROJECT_ROOT"
        docker compose up -d postgres quizz
    )
}

generate_password_hash() {
    PASSWORD_HASH="$(
        cd "$PROJECT_ROOT"
        printf '%s' "$ADMIN_PASSWORD" | docker compose run --rm --no-deps quizz hash-password
    )"

    unset ADMIN_PASSWORD
    unset ADMIN_PASSWORD_CONFIRM

    if [[ -z "$PASSWORD_HASH" ]]; then
        printf 'Password hash generation failed.\n' >&2
        exit 1
    fi
}

wait_for_postgres() {
    local attempts=60

    while (( attempts > 0 )); do
        if (
            cd "$PROJECT_ROOT"
            docker compose exec -T postgres pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB" >/dev/null 2>&1
        ); then
            return
        fi

        attempts=$((attempts - 1))
        sleep 2
    done

    printf 'Timed out waiting for PostgreSQL readiness.\n' >&2
    exit 1
}

wait_for_users_table() {
    local attempts=60
    local table_name

    while (( attempts > 0 )); do
        table_name="$(
            cd "$PROJECT_ROOT"
            docker compose exec -T postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -tAc "SELECT to_regclass('public.users');" 2>/dev/null || true
        )"

        if [[ "$table_name" == "users" || "$table_name" == "public.users" ]]; then
            return
        fi

        attempts=$((attempts - 1))
        sleep 2
    done

    printf 'Timed out waiting for Flyway to create the users table.\n' >&2
    exit 1
}

upsert_admin() {
    local sql_file="$PROJECT_ROOT/scripts/sql/upsert-admin.sql"

    if [[ ! -f "$sql_file" ]]; then
        printf 'Missing SQL file: scripts/sql/upsert-admin.sql\n' >&2
        exit 1
    fi

    (
        cd "$PROJECT_ROOT"
        docker compose exec -T postgres \
            psql -v ON_ERROR_STOP=1 \
                 -U "$POSTGRES_USER" \
                 -d "$POSTGRES_DB" \
                 -v admin_email="$ADMIN_EMAIL" \
                 -v admin_full_name="$ADMIN_FULL_NAME" \
                 -v password_hash="$PASSWORD_HASH" \
            < "$sql_file"
    )
}

main() {
    require_command docker
    resolve_project_root
    read_admin_input
    ensure_secret_file_exists
    ensure_compose_services
    wait_for_postgres
    wait_for_users_table
    generate_password_hash
    upsert_admin

    printf 'Admin bootstrap complete.\n'
    printf 'Email: %s\n' "$ADMIN_EMAIL"
    printf 'Role: ADMIN\n'
    printf 'Enabled: true\n'
}

main "$@"
