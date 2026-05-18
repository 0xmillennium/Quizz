# Scripts

## bootstrap-admin.sh

Interactive Docker-friendly admin bootstrap script. It reads admin identity from `.env`, prompts only for the password, generates a BCrypt hash through the Quizz Docker image, then applies `scripts/sql/upsert-admin.sql` through `docker compose exec` against the private PostgreSQL container.

Before running Docker or scripts, create and review the non-secret local config:

```bash
cp .env.example .env
```

Do not put passwords, tokens, API keys, or database secrets in `.env`. `QUIZZ_HTTP_PORT` controls the host port, and tooling derives `http://localhost:<QUIZZ_HTTP_PORT>`. Do not define `QUIZZ_BASE_URL`. The database password remains a Docker Compose secret, and the admin password is always prompted interactively with hidden input.

Admin bootstrap is intentionally database-backed because it solves the first-admin problem: no admin user may exist yet, so no protected admin endpoint can be used.

The bootstrap script reads `QUIZZ_DEFAULT_ADMIN_EMAIL`, `QUIZZ_DEFAULT_ADMIN_FULL_NAME`, `POSTGRES_DB`, and `POSTGRES_USER` from `.env`.

## sql/upsert-admin.sql

Idempotent PostgreSQL script for inserting or updating one admin account. It uses psql variables for the admin email, full name, and password hash.

## demo

Endpoint-based demo catalog fixture scripts live under `scripts/demo`.

- `load-all-demo-catalog.py` loads categories, questions, quizzes, and publishes configured quizzes.
- `load-demo-categories.py` loads only demo categories.
- `load-demo-questions.py` loads only demo questions.
- `load-demo-quizzes.py` loads only demo quizzes and publishes configured quizzes.

Unlike admin bootstrap, demo fixture scripts must use the application over HTTP. They log in through `/login`, keep session cookies, extract CSRF tokens from forms, submit the existing admin MVC endpoints, and do not access PostgreSQL directly.

Demo scripts read `QUIZZ_HTTP_PORT` and `QUIZZ_DEFAULT_ADMIN_EMAIL` from `.env`. They do not accept `--base-url` or `--admin-email`; the base URL is always derived as `http://localhost:<QUIZZ_HTTP_PORT>`.

See `docs/DEMO_FIXTURES.md` for commands, idempotency rules, and troubleshooting.
