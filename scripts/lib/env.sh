#!/usr/bin/env bash

FORBIDDEN_ENV_KEYS=(
    POSTGRES_PASSWORD
    SPRING_DATASOURCE_PASSWORD
    ADMIN_PASSWORD
    QUIZZ_ADMIN_PASSWORD
)

resolve_project_root() {
    local start_dir="${1:-}"

    if [[ -z "$start_dir" ]]; then
        start_dir="$(pwd)"
    fi

    start_dir="$(cd "$start_dir" && pwd)"

    while [[ "$start_dir" != "/" ]]; do
        if [[ -f "$start_dir/pom.xml" && -f "$start_dir/docker-compose.yml" ]]; then
            printf '%s\n' "$start_dir"
            return
        fi
        start_dir="$(dirname "$start_dir")"
    done

    printf 'ERROR: Could not resolve project root.\n' >&2
    exit 1
}

load_required_env() {
    local project_root="$1"
    shift

    local env_file="$project_root/.env"
    local -A env_values=()
    local -A seen_keys=()
    local line key value
    local line_number=0

    if [[ ! -f "$env_file" ]]; then
        printf 'ERROR: .env file not found.\n' >&2
        printf 'Create it with:\n' >&2
        printf '  cp .env.example .env\n' >&2
        printf 'Then review values before running this script.\n' >&2
        exit 1
    fi

    while IFS= read -r line || [[ -n "$line" ]]; do
        line_number=$((line_number + 1))
        line="${line%$'\r'}"

        if [[ "$line" =~ ^[[:space:]]*$ || "$line" =~ ^[[:space:]]*# ]]; then
            continue
        fi

        if [[ "$line" =~ ^[[:space:]]*export[[:space:]]+ ]]; then
            printf 'ERROR: Unsupported .env syntax on line %d: export is not allowed.\n' "$line_number" >&2
            exit 1
        fi

        if [[ ! "$line" =~ ^([A-Za-z_][A-Za-z0-9_]*)=(.*)$ ]]; then
            printf 'ERROR: Unsupported .env syntax on line %d. Use KEY=value.\n' "$line_number" >&2
            exit 1
        fi

        key="${BASH_REMATCH[1]}"
        value="${BASH_REMATCH[2]}"

        if [[ -n "${seen_keys[$key]:-}" ]]; then
            printf 'ERROR: Duplicate .env key: %s\n' "$key" >&2
            exit 1
        fi
        seen_keys["$key"]=1

        if [[ "$value" == *'$('* || "$value" == *'`'* ]]; then
            printf 'ERROR: Unsupported .env value for %s: command substitution is not allowed.\n' "$key" >&2
            exit 1
        fi

        if [[ "$value" =~ ^\"(.*)\"$ ]]; then
            value="${BASH_REMATCH[1]}"
        elif [[ "$value" =~ ^\'(.*)\'$ ]]; then
            value="${BASH_REMATCH[1]}"
        fi

        env_values["$key"]="$value"
    done < "$env_file"

    reject_forbidden_env_keys env_values

    local required_key
    for required_key in "$@"; do
        require_env_key env_values "$required_key"
        printf -v "$required_key" '%s' "${env_values[$required_key]}"
        export "$required_key"
    done
}

require_env_key() {
    local -n values_ref="$1"
    local key="$2"

    if [[ ! -v values_ref[$key] ]]; then
        printf 'ERROR: Missing required .env key: %s\n' "$key" >&2
        exit 1
    fi

    if [[ -z "${values_ref[$key]}" ]]; then
        printf 'ERROR: Required .env key is blank: %s\n' "$key" >&2
        exit 1
    fi
}

reject_forbidden_env_keys() {
    local -n values_ref="$1"
    local forbidden_key

    if [[ -v values_ref[QUIZZ_BASE_URL] ]]; then
        printf 'ERROR: QUIZZ_BASE_URL is not supported. Configure QUIZZ_HTTP_PORT instead.\n' >&2
        exit 1
    fi

    for forbidden_key in "${FORBIDDEN_ENV_KEYS[@]}"; do
        if [[ -v values_ref[$forbidden_key] ]]; then
            printf 'ERROR: %s must not be stored in .env. Use Docker secrets.\n' "$forbidden_key" >&2
            exit 1
        fi
    done
}
