# Docker Runtime

Docker Compose defines a local two-service runtime: the Quizz application and PostgreSQL.

## Services

| Service | Purpose |
| --- | --- |
| `quizz` | Spring Boot MVC application built from the repository Dockerfile. |
| `postgres` | PostgreSQL 17 database with persistent local volume. |

## Networks

| Network | Purpose |
| --- | --- |
| `quizz_frontend` | Bridge network for the application service and host-published HTTP port. |
| `quizz_backend` | Internal bridge network shared by application and PostgreSQL. |

PostgreSQL is not published to the host by Compose. The application connects to it over the internal backend network.

## Volumes and Secrets

- `quizz_postgres_data` stores PostgreSQL data.
- `postgres_password` is mounted into PostgreSQL as `/run/secrets/postgres_password`.
- The same secret is mounted into the app as `/run/secrets/spring.datasource.password`.

## Published Port

The app container listens on container port `8080`. Compose publishes it to the host port configured by `QUIZZ_HTTP_PORT`.

## Healthchecks

- PostgreSQL uses `pg_isready`.
- The app uses the actuator health endpoint on its container-local HTTP port.
- `quizz` depends on `postgres` becoming healthy before startup.

## Runtime Hardening

The app runtime image:

- Uses a multi-stage Dockerfile.
- Copies only the built jar into the runtime image.
- Runs as a non-root user.
- Uses a read-only filesystem with `/tmp` as tmpfs.
- Drops Linux capabilities.
- Enables `no-new-privileges`.

The PostgreSQL service also uses `no-new-privileges` and JSON-file log rotation.
