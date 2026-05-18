# Demo Fixtures

## Purpose

The demo fixture tooling loads a reusable catalog of demo categories, questions, and quizzes into a running Quizz application. It uses the same Spring MVC admin pages an administrator uses in the browser: real login, session cookies, CSRF tokens, form submissions, and redirects.

This tooling is for catalog content only. It does not create demo users, solve quizzes, or generate attempts.

## Prerequisites

- Create and review `.env`:

```bash
cp .env.example .env
```

- The Quizz application is running, for example through Docker.
- An admin account has already been bootstrapped.
- The admin account can log in through `/login` and access `/admin`.

The `.env` file is non-secret local config. Do not put passwords, tokens, API keys, or database secrets in it. The database password remains a Docker Compose secret, and the admin password is prompted interactively.

`QUIZZ_HTTP_PORT` controls the host port. Demo tooling derives the base URL as `http://localhost:<QUIZZ_HTTP_PORT>`. With the default `.env.example` value, that is `http://localhost:8081`. Do not define `QUIZZ_BASE_URL`.

## Load All Demo Catalog Data

```bash
python3 scripts/demo/load-all-demo-catalog.py
```

The script reads `QUIZZ_HTTP_PORT` and `QUIZZ_DEFAULT_ADMIN_EMAIL` from `.env`, then prompts for the admin password with hidden input. Passwords are not accepted through command-line arguments or environment variables. Demo scripts no longer accept `--base-url` or `--admin-email`.

## Individual Commands

Load categories:

```bash
python3 scripts/demo/load-demo-categories.py
```

Load questions:

```bash
python3 scripts/demo/load-demo-questions.py
```

Load quizzes:

```bash
python3 scripts/demo/load-demo-quizzes.py
```

Questions require the referenced categories to already exist. Quizzes require the referenced categories and questions to already exist.

The bundled catalog contains five categories, at least twelve questions per category, four options per question, and one published quiz per category. Quiz fixtures define a question pool plus:

- `questionCount`: questions sampled per attempt.
- `attemptLimit`: attempt rights per cooldown window.
- `retakeCooldownMinutes`: cooldown duration after rights are used.

## Idempotency

- Category with the same exact name: skipped.
- Question with the same exact text: skipped.
- Quiz with the same exact title and status `PUBLISHED`: skipped.
- Quiz with the same exact title and status `DRAFT`: published when the fixture has `publish=true`; otherwise skipped.
- Quiz with the same exact title and status `ARCHIVED`: warning and skipped.

Existing content is not edited, deleted, reset, or overwritten.

## Security Notes

- Uses the real `/login` endpoint.
- Maintains session cookies with a cookie jar.
- Extracts CSRF hidden inputs from HTML and submits them with forms.
- Uses existing admin MVC endpoints only.
- Does not use direct database access.
- Does not use SQL for demo fixtures.
- Does not store or log the admin password.
- Does not disable or bypass Spring Security.

## Troubleshooting

Login failed:
Verify `QUIZZ_DEFAULT_ADMIN_EMAIL` in `.env`, the prompted password, and the ADMIN role.

CSRF token not found:
Confirm the app is serving the expected MVC pages and that you are using the correct base URL. A proxy or error page may be returning non-form HTML.

Missing category:
Run the category loader first or use `load-all-demo-catalog.py`. The scripts intentionally fail when dependencies are missing.

Missing question:
Run the question loader first or use `load-all-demo-catalog.py`. Quiz fixtures reference question text exactly.

Quiz archived:
Archived quizzes are not edited or republished by this phase. Rename the fixture title or handle the archived quiz manually.

Wrong port or base URL:
Set `QUIZZ_HTTP_PORT` in `.env`. Tooling derives `http://localhost:<QUIZZ_HTTP_PORT>` and rejects `QUIZZ_BASE_URL`.
