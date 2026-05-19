# Demo Fixtures

Demo fixture scripts load catalog data through admin HTTP endpoints. They do not write directly to the database.

## Purpose

Use demo fixtures to create a useful local catalog for development and manual testing. The scripts create categories, questions, and quizzes through the same CSRF-protected MVC forms used by admins.

## Prerequisites

- The application is running.
- An admin account exists.
- `.env` exists.
- The admin password is known to the person running the script.

## Scripts

```bash
python3 scripts/demo/load-all-demo-catalog.py
python3 scripts/demo/load-demo-categories.py
python3 scripts/demo/load-demo-questions.py
python3 scripts/demo/load-demo-quizzes.py
```

Each script prompts for the admin password, signs in through `/login`, extracts CSRF tokens from forms, and submits admin forms over HTTP.

## Data Created

- Categories from `scripts/demo/data/categories.json`
- Questions and answer options from `scripts/demo/data/questions.json`
- Quizzes from `scripts/demo/data/quizzes.json`
- Quiz policies such as question count, attempt limit, and retake cooldown

Quiz attempts created later use randomized pools and immutable snapshots from the published quiz definitions.

## Idempotency

The scripts inspect admin list pages before creating records. Existing categories and questions are skipped by name/text. Existing published quizzes are skipped; existing draft quizzes may be published when the fixture requests publication.

## Troubleshooting

- Authentication failure: verify the admin email in `.env` and the password entered at the prompt.
- Missing category for questions: load categories first or run `load-all-demo-catalog.py`.
- Missing question for quizzes: load questions first or run `load-all-demo-catalog.py`.
- CSRF token not found: verify the application is serving the expected MVC forms and that the logged-in account has admin access.
