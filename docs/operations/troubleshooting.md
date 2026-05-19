# Troubleshooting

Use the pattern below to identify the failure, fix the cause, and verify the result.

## Env File Not Found

| Field | Details |
| --- | --- |
| Symptom | Scripts print `ERROR: .env file not found.` |
| Likely cause | `.env` has not been created from `.env.example`. |
| Fix | Run `cp .env.example .env` and review the values. |
| Verification command | `docker compose config` |

## Unsupported QUIZZ_BASE_URL

| Field | Details |
| --- | --- |
| Symptom | Scripts reject `QUIZZ_BASE_URL`. |
| Likely cause | `.env` contains an unsupported base URL key. |
| Fix | Remove `QUIZZ_BASE_URL` and configure `QUIZZ_HTTP_PORT`. |
| Verification command | `python3 scripts/demo/load-demo-categories.py --help` |

## Docker Secret Missing

| Field | Details |
| --- | --- |
| Symptom | Compose or bootstrap reports `docker/secrets/postgres_password.txt` is missing. |
| Likely cause | The local PostgreSQL password secret file has not been generated. |
| Fix | Generate the secret file with OpenSSL and set restrictive permissions. |
| Verification command | `test -f docker/secrets/postgres_password.txt` |

## Port Already Allocated

| Field | Details |
| --- | --- |
| Symptom | Compose cannot publish the application port. |
| Likely cause | Another process is using `QUIZZ_HTTP_PORT`. |
| Fix | Stop the process or change `QUIZZ_HTTP_PORT` in `.env`. |
| Verification command | `docker compose config` |

## PostgreSQL Unhealthy

| Field | Details |
| --- | --- |
| Symptom | `postgres` remains unhealthy. |
| Likely cause | Bad secret, stale volume, or database initialization failure. |
| Fix | Check logs and recreate the local database volume when the data can be discarded. |
| Verification command | `docker compose logs postgres` |

## App Health Down

| Field | Details |
| --- | --- |
| Symptom | `quizz` is unhealthy or `/actuator/health` does not return success. |
| Likely cause | Database connection, Flyway, schema validation, or application startup failure. |
| Fix | Check app logs and verify PostgreSQL is healthy. |
| Verification command | `docker compose logs quizz` |

## Flyway Or Schema Validation Failure

| Field | Details |
| --- | --- |
| Symptom | Application startup fails during migration or Hibernate validation. |
| Likely cause | Database schema does not match Flyway migrations or the volume contains incompatible local state. |
| Fix | Inspect logs. For disposable local data, recreate the PostgreSQL volume. |
| Verification command | `docker compose logs quizz` |

## Stale Docker Volume After Schema Changes

| Field | Details |
| --- | --- |
| Symptom | Local runtime fails after schema-related code changes. |
| Likely cause | Existing `quizz_postgres_data` contains old schema state. |
| Fix | Stop Compose and recreate the local database volume if the data can be discarded. |
| Verification command | `docker volume ls | grep quizz_postgres_data` |

## Admin Bootstrap Fails

| Field | Details |
| --- | --- |
| Symptom | `scripts/bootstrap-admin.sh` exits before printing completion. |
| Likely cause | Missing `.env`, missing secret, Docker unavailable, PostgreSQL not ready, or password validation failure. |
| Fix | Resolve the first error printed by the script and rerun it. |
| Verification command | `scripts/bootstrap-admin.sh` |

## Login Fails

| Field | Details |
| --- | --- |
| Symptom | Admin login redirects back to `/login?error`. |
| Likely cause | Wrong password, wrong admin email in `.env`, disabled user, or missing admin role. |
| Fix | Rerun admin bootstrap and use the configured admin email. |
| Verification command | Open `/admin` after login. |

## Demo Fixture Script Fails

| Field | Details |
| --- | --- |
| Symptom | Demo script prints an authentication, CSRF, missing category, or missing question error. |
| Likely cause | App not running, admin account missing, wrong password, or prerequisites loaded out of order. |
| Fix | Bootstrap admin, then run `load-all-demo-catalog.py`. |
| Verification command | `python3 scripts/demo/load-all-demo-catalog.py` |

## CSRF Error

| Field | Details |
| --- | --- |
| Symptom | Form POST fails with a forbidden response. |
| Likely cause | Missing or stale CSRF token. |
| Fix | Reload the form and submit again. Demo scripts fetch forms before POSTing. |
| Verification command | Repeat the form submission after refresh. |

## MkDocs Build Fails

| Field | Details |
| --- | --- |
| Symptom | `mkdocs build --strict` exits with an error. |
| Likely cause | Missing docs dependency, broken nav entry, or broken link. |
| Fix | Install requirements and fix the reported path or link. |
| Verification command | `mkdocs build --strict` |
