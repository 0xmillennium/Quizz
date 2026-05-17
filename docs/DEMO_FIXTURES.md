# Demo Fixtures

## Purpose

The demo fixture tooling loads a reusable catalog of demo categories, questions, and quizzes into a running Quizz application. It uses the same Spring MVC admin pages an administrator uses in the browser: real login, session cookies, CSRF tokens, form submissions, and redirects.

This tooling is for catalog content only. It does not create demo users, solve quizzes, or generate attempts.

## Prerequisites

- The Quizz application is running, for example through Docker.
- An admin account has already been bootstrapped.
- The admin account can log in through `/login` and access `/admin`.

For Docker, the app is commonly available at `http://localhost:8081`. For a local Spring Boot run, it may be `http://localhost:8080`.

## Load All Demo Catalog Data

```bash
python3 scripts/demo/load-all-demo-catalog.py --base-url http://localhost:8080
```

The script prompts for the admin password with hidden input. Passwords are not accepted through command-line arguments or environment variables.

## Base URL Override

Base URL resolution order:

1. `--base-url`
2. `QUIZZ_BASE_URL`
3. `http://localhost:8080`

Example:

```bash
QUIZZ_BASE_URL=http://localhost:8081 python3 scripts/demo/load-all-demo-catalog.py
```

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
Verify the admin email, password, and ADMIN role. The default email is `admin@example.com`; override it with `--admin-email`.

CSRF token not found:
Confirm the app is serving the expected MVC pages and that you are using the correct base URL. A proxy or error page may be returning non-form HTML.

Missing category:
Run the category loader first or use `load-all-demo-catalog.py`. The scripts intentionally fail when dependencies are missing.

Missing question:
Run the question loader first or use `load-all-demo-catalog.py`. Quiz fixtures reference question text exactly.

Quiz archived:
Archived quizzes are not edited or republished by this phase. Rename the fixture title or handle the archived quiz manually.

Wrong port or base URL:
Use `--base-url` or `QUIZZ_BASE_URL`. Docker may use `http://localhost:8081`; local Spring Boot often uses `http://localhost:8080`.
