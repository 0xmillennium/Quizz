# Docker Runtime

## Purpose

This is the secure local Docker runtime for Quizz. It starts the Spring Boot MVC application and a private PostgreSQL database with Docker Compose.

## Prerequisites

- Docker
- Docker Compose plugin
- Java and Maven are only needed for non-Docker local workflows. They are not required to run the Compose image.

## Secret Setup

Create the local PostgreSQL password secret manually:

```bash
mkdir -p docker/secrets
openssl rand -base64 48 | tr -d '\n' > docker/secrets/postgres_password.txt
chmod 600 docker/secrets/postgres_password.txt
```

The secret file is ignored by git. Do not commit real secrets.

Docker Compose mounts the password as a Docker secret. PostgreSQL reads it through `POSTGRES_PASSWORD_FILE`. Quizz reads the same secret through Spring Boot configtree as `spring.datasource.password`. The Quizz image runs as fixed non-root UID/GID `10001:10001`.

## Start

```bash
docker compose up --build
```

## Verify

```bash
docker compose ps
curl http://localhost:8080/actuator/health
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

- `quizz_frontend` is the bridge network used for host access to the Quizz app on port `8080`.
- `quizz_backend` is an internal bridge network used for private app-to-database traffic.
- `postgres` has no host port and is attached only to `quizz_backend`.
- `quizz` reaches PostgreSQL by service name: `postgres`.

## Confirm DB Is Not Exposed

```bash
docker compose ps
```

The output should not show port `5432` published. `localhost:5432` should not be available from the host through this Compose runtime.

## Admin Bootstrap

Connect to PostgreSQL inside the Docker network:

```bash
docker compose exec postgres psql -U quizz -d quizz
```

Then follow [Admin bootstrap](ADMIN_BOOTSTRAP.md). Do not add a `DevDataInitializer`.

## Troubleshooting

- Missing secret file: create `docker/secrets/postgres_password.txt` using the commands above.
- Java image tag or build failure: confirm Docker can pull `maven:3.9.11-eclipse-temurin-25` and `eclipse-temurin:25-jre-alpine-3.22`.
- PostgreSQL healthcheck failure: inspect `docker compose logs -f postgres` and confirm the secret file exists.
- Flyway migration failure: inspect `docker compose logs -f quizz`; schema changes must come from migrations.
- Port `8080` already in use: stop the process using that host port or adjust the host-side published port locally.

## Future CI/CD Notes

A future pipeline can add this sequence:

```bash
mvn clean test
docker compose config
docker compose build
```

An optional container smoke test can start the stack and query `/actuator/health`. This phase does not add CI/CD configuration.
