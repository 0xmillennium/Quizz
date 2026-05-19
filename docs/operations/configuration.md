# Configuration

Quizz uses `.env` as the single source for non-secret local configuration. Docker Compose interpolates these values, and local scripts validate the same file.

## Non-secret Local Configuration

Required `.env` keys:

- `QUIZZ_HTTP_PORT`
- `QUIZZ_DEFAULT_ADMIN_EMAIL`
- `QUIZZ_DEFAULT_ADMIN_FULL_NAME`
- `POSTGRES_DB`
- `POSTGRES_USER`

`QUIZZ_BASE_URL` is not supported. Local tooling derives the base URL as:

```text
http://localhost:<QUIZZ_HTTP_PORT>
```

## What Belongs in `.env`

- Local port selection.
- Non-secret admin identity defaults.
- Non-secret PostgreSQL database and username values.

## What Must Not Be in `.env`

- Database passwords.
- Admin passwords.
- API keys.
- Tokens.
- Password hashes.

The env loaders reject `POSTGRES_PASSWORD`, `SPRING_DATASOURCE_PASSWORD`, `ADMIN_PASSWORD`, `QUIZZ_ADMIN_PASSWORD`, and `QUIZZ_BASE_URL`.

## Docker Compose Interpolation

Compose reads `.env` and uses it for:

- Published application port: `${QUIZZ_HTTP_PORT}:8080`
- Database name: `${POSTGRES_DB}`
- Database username: `${POSTGRES_USER}`
- Application datasource URL and username

The database password is not interpolated from `.env`; it is mounted as a Docker secret.

## Spring Profiles

| Profile | Used by | Notes |
| --- | --- | --- |
| `dev` | Default application profile | Direct Maven/local PostgreSQL development. |
| `test` | Tests | Keeps JPA validation and Flyway enabled for test context. |
| `docker` | Docker Compose | Imports config tree from `/run/secrets/` and reads datasource password from the mounted secret. |

## Config Tree and Docker Secrets

The Docker profile imports `optional:configtree:/run/secrets/`. Compose mounts the PostgreSQL password secret for the application at `/run/secrets/spring.datasource.password`, which Spring maps to `spring.datasource.password`.
