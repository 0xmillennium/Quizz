# Scripts

## bootstrap-admin.sh

Interactive Docker-friendly admin bootstrap script. It prompts for admin identity and password, generates a BCrypt hash through the Quizz Docker image, then applies `scripts/sql/upsert-admin.sql` through `docker compose exec` against the private PostgreSQL container.

Admin bootstrap is intentionally database-backed because it solves the first-admin problem: no admin user may exist yet, so no protected admin endpoint can be used.

## sql/upsert-admin.sql

Idempotent PostgreSQL script for inserting or updating one admin account. It uses psql variables for the admin email, full name, and password hash.

## demo

Endpoint-based demo catalog fixture scripts live under `scripts/demo`.

- `load-all-demo-catalog.py` loads categories, questions, quizzes, and publishes configured quizzes.
- `load-demo-categories.py` loads only demo categories.
- `load-demo-questions.py` loads only demo questions.
- `load-demo-quizzes.py` loads only demo quizzes and publishes configured quizzes.

Unlike admin bootstrap, demo fixture scripts must use the application over HTTP. They log in through `/login`, keep session cookies, extract CSRF tokens from forms, submit the existing admin MVC endpoints, and do not access PostgreSQL directly.

See `docs/DEMO_FIXTURES.md` for commands, idempotency rules, and troubleshooting.
