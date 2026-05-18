# Docker Runtime

## Purpose

This is the secure local Docker runtime for Quizz. It starts the Spring Boot MVC application and a private PostgreSQL database with Docker Compose.

## Prerequisites

- Docker
- Docker Compose plugin
- Java and Maven are only needed for non-Docker local workflows. They are not required to run the Compose image.

## Secret Setup

Create the non-secret local configuration first:

```bash
cp .env.example .env
```

Review `.env` before starting Docker. It controls `QUIZZ_HTTP_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, and default admin prompt values. It is intentionally non-secret local config only. Do not put passwords, tokens, API keys, or database secrets in `.env`, and do not define `QUIZZ_BASE_URL`.

Create the local PostgreSQL password secret manually:

```bash
mkdir -p docker/secrets
openssl rand -base64 48 | tr -d '\n' > docker/secrets/postgres_password.txt
chmod 600 docker/secrets/postgres_password.txt
```

The secret file is ignored by git. Do not commit real secrets.

Docker Compose mounts the password as a Docker secret. PostgreSQL reads it through `POSTGRES_PASSWORD_FILE`. Quizz reads the same secret through Spring Boot configtree as `spring.datasource.password`. The Quizz image runs as fixed non-root UID/GID `10001:10001`.

The host application port comes from `QUIZZ_HTTP_PORT`. Tooling derives the local base URL as `http://localhost:<QUIZZ_HTTP_PORT>`. With the default `.env.example` value, that is `http://localhost:8081`.

## Start

```bash
docker compose up --build
```

After the stack is up and Flyway has created the schema, bootstrap an admin account:

```bash
./scripts/bootstrap-admin.sh
```

## Verify

```bash
docker compose ps
curl http://localhost:<QUIZZ_HTTP_PORT from .env>/actuator/health
```

Expected:

- `postgres` is healthy.
- `quizz` is healthy.
- `/actuator/health` returns `UP`.

## Logs

```bash
docker compose logs -f quizz
docker compose logs -f postgres
```

## Stop

```bash
docker compose down
```

## Reset DB Volume

```bash
docker compose down -v
```

Warning: this deletes the PostgreSQL data volume.

## Network Layout

- `quizz_frontend` is the bridge network used for host access to the Quizz app on host port `QUIZZ_HTTP_PORT` from `.env`.
- `quizz_backend` is an internal bridge network used for private app-to-database traffic.
- `postgres` has no host port and is attached only to `quizz_backend`.
- `quizz` reaches PostgreSQL by service name: `postgres`.

## Confirm DB Is Not Exposed

```bash
docker compose ps
```

The output should not show port `5432` published. `localhost:5432` should not be available from the host through this Compose runtime.

## Admin Bootstrap

Use the bootstrap script:

```bash
./scripts/bootstrap-admin.sh
```

The script is idempotent. It reads admin email and full name from `.env`, prompts only for the password, sends the password through stdin to the Quizz image `hash-password` CLI mode, and applies `scripts/sql/upsert-admin.sql` with `docker compose exec -T postgres psql`.

The script reads its non-secret defaults and PostgreSQL database/user names from `.env`. The admin password is always prompted interactively with hidden input. The script does not require a PostgreSQL host port, does not use HTTP, and does not require local Java or Maven. Do not add a `DevDataInitializer`.

More detail is in [Admin bootstrap](ADMIN_BOOTSTRAP.md).

## Troubleshooting

- Missing secret file: create `docker/secrets/postgres_password.txt` using the commands above.
- Java image tag or build failure: confirm Docker can pull `maven:3.9.11-eclipse-temurin-25` and `eclipse-temurin:25-jre-alpine-3.22`.
- PostgreSQL healthcheck failure: inspect `docker compose logs -f postgres` and confirm the secret file exists.
- Flyway migration failure: inspect `docker compose logs -f quizz`; schema changes must come from migrations.
- Host port already in use: change `QUIZZ_HTTP_PORT` in `.env`, then use `http://localhost:<QUIZZ_HTTP_PORT>`.

## Future CI/CD Notes

A future pipeline can add this sequence:

```bash
mvn clean test
docker compose config
docker compose build
```

An optional container smoke test can start the stack and query `/actuator/health`. This phase does not add CI/CD configuration.
