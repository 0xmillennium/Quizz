# Scripts and Tooling Reference

## `scripts/bootstrap-admin.sh`

| Field | Details |
| --- | --- |
| Purpose | Create or update the configured admin account. |
| Prerequisites | `.env`, Docker, Docker secret file, Compose runtime availability. |
| Inputs | Admin email and full name from `.env`; admin password from interactive prompt. |
| Outputs | Admin user id from SQL, completion message, admin email, role, enabled status. |
| Safety properties | Password is prompted, not passed as argv; hash generation uses stdin/stdout/stderr; script unsets the password variables; SQL upserts one configured admin. |
| Command | `scripts/bootstrap-admin.sh` |

Admin bootstrap is the controlled direct database exception. It waits for Flyway to create the `users` table, generates a bcrypt hash through application tooling, and runs `scripts/sql/upsert-admin.sql`.

## `PasswordHashCli` / `hash-password`

| Field | Details |
| --- | --- |
| Purpose | Generate a bcrypt hash from a password without starting the Spring context. |
| Prerequisites | Built application jar or Docker image. |
| Inputs | Password on standard input. |
| Outputs | Hash on standard output; validation errors on standard error. |
| Safety properties | Password is not accepted as a command-line argument. |
| Command | `docker compose run --rm --no-deps quizz hash-password` |

## `scripts/demo/load-all-demo-catalog.py`

| Field | Details |
| --- | --- |
| Purpose | Load categories, questions, and quizzes through admin HTTP endpoints. |
| Prerequisites | App running, admin exists, `.env` exists, admin password known. |
| Inputs | Fixture JSON files and interactive admin password prompt. |
| Outputs | Per-record created/skipped/published/warning results. |
| Safety properties | Uses MVC endpoints and CSRF tokens; no direct database writes. |
| Command | `python3 scripts/demo/load-all-demo-catalog.py` |

## `scripts/demo/load-demo-categories.py`

| Field | Details |
| --- | --- |
| Purpose | Load demo categories through admin HTTP endpoints. |
| Prerequisites | App running and admin login available. |
| Inputs | `scripts/demo/data/categories.json` and interactive admin password prompt. |
| Outputs | Per-category results. |
| Safety properties | Skips existing categories by name; no direct database writes. |
| Command | `python3 scripts/demo/load-demo-categories.py` |

## `scripts/demo/load-demo-questions.py`

| Field | Details |
| --- | --- |
| Purpose | Load demo questions through admin HTTP endpoints. |
| Prerequisites | Categories exist, app running, admin login available. |
| Inputs | `scripts/demo/data/questions.json` and interactive admin password prompt. |
| Outputs | Per-question results. |
| Safety properties | Skips existing questions by text; no direct database writes. |
| Command | `python3 scripts/demo/load-demo-questions.py` |

## `scripts/demo/load-demo-quizzes.py`

| Field | Details |
| --- | --- |
| Purpose | Load demo quizzes through admin HTTP endpoints. |
| Prerequisites | Categories and questions exist, app running, admin login available. |
| Inputs | `scripts/demo/data/quizzes.json` and interactive admin password prompt. |
| Outputs | Per-quiz created/skipped/published/warning results. |
| Safety properties | Skips published quizzes by title; can publish an existing draft when fixture data requires it; no direct database writes. |
| Command | `python3 scripts/demo/load-demo-quizzes.py` |
