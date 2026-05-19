# Configuration Reference

## `.env` Keys

| Key | Required | Secret? | Example | Purpose |
| --- | --- | --- | --- | --- |
| `QUIZZ_HTTP_PORT` | Yes | No | `8081` | Host port published to the Quizz application container. |
| `QUIZZ_DEFAULT_ADMIN_EMAIL` | Yes | No | `admin@example.com` | Admin email used by bootstrap and demo scripts. |
| `QUIZZ_DEFAULT_ADMIN_FULL_NAME` | Yes | No | `Admin User` | Admin display name used by bootstrap. |
| `POSTGRES_DB` | Yes | No | `quizz` | PostgreSQL database name. |
| `POSTGRES_USER` | Yes | No | `quizz` | PostgreSQL database user. |

## Docker Secret

| Secret | File | Mounted for | Purpose |
| --- | --- | --- | --- |
| `postgres_password` | `docker/secrets/postgres_password.txt` | `postgres`, `quizz` | PostgreSQL password. |

The application receives the secret as `spring.datasource.password` through Spring config tree import in the `docker` profile.

## Spring Profiles

| Profile | Source | Purpose |
| --- | --- | --- |
| `dev` | `application-dev.yml` | Direct local Maven development. |
| `test` | `application-test.yml` | Test runtime settings. |
| `docker` | `application-docker.yml` | Docker Compose runtime with config tree secrets. |

## Unsupported Keys

| Key | Status | Use instead |
| --- | --- | --- |
| `QUIZZ_BASE_URL` | Unsupported and rejected by local tooling | `QUIZZ_HTTP_PORT` |

## Derived Values

| Value | Derived from |
| --- | --- |
| Local app base URL | `http://localhost:<QUIZZ_HTTP_PORT>` |
| Docker datasource URL | `jdbc:postgresql://postgres:5432/${POSTGRES_DB}` |
| Docker datasource username | `${POSTGRES_USER}` |
