# Contributing to Quizz

## Project Scope

Quizz is a Spring Boot MVC quiz platform for server-rendered quiz authoring, quiz taking, scoring, leaderboards, and admin reporting. The project uses Java 25, Maven, Thymeleaf, Spring Security with CSRF, PostgreSQL, Flyway, Docker Compose, and MkDocs.

This repository does not use Lombok, a frontend framework, or an npm application build pipeline.

## Code of Conduct

All contributors are expected to follow [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md). Keep discussion respectful, focused on the work, and welcoming to people with different levels of project context.

## Security Reporting

Do not report vulnerabilities in public issues. Follow [SECURITY.md](SECURITY.md) for private reporting guidance.

Security-sensitive examples include authentication/session bypass, CSRF bypass, admin authorization bypass, correct answer leakage on play pages, `originalAnswerOptionId` leakage on play views, password hash exposure, user email/privacy exposure, Docker secret leakage, and unsafe GitHub Actions permission escalation.

## Development Setup

Use the documented Docker or local development flow in `README.md` and `docs/getting-started/`.

For local repository hygiene tooling:

```bash
python3 -m pip install pre-commit
pre-commit install
pre-commit run --all-files
```

Pre-commit is a local convenience. CI remains the final source of truth.

## Default Branch and Contribution Flow

The default branch is `master`.

Do not commit directly to `master`. Every change should be made on a focused, short-lived side branch and proposed through a pull request targeting `master`.

Contribution flow:

1. Create a side branch.
2. Commit focused changes.
3. Open a pull request targeting `master`.
4. Pass required checks.
5. Maintainers squash and merge into `master`.

## Branch Naming

Prefer short, descriptive branch names:

- `feature/quiz-filtering`
- `fix/attempt-autosave`
- `docs/docker-secrets`
- `ci/mkdocs-audit`
- `chore/dependency-rules`
- `security/csrf-hardening`

## PR Title / Conventional Commits

Use Conventional Commit style for PR titles. Maintainers should squash merge, so the PR title is the primary commit message that must pass convention checks. The final squash commit title should preserve the validated PR title.

Valid PR title examples:

- `feat(quiz): add randomized pool validation`
- `fix(attempt): prevent stale autosave overwrite`
- `docs(security): clarify vulnerability reporting`
- `ci(docker): add compose build validation`

Invalid PR title examples:

- `Update stuff`
- `Fix bug`
- `changes`

Allowed types are `feat`, `fix`, `docs`, `test`, `refactor`, `style`, `build`, `ci`, `chore`, `perf`, and `security`.

Recommended scopes are `auth`, `user`, `category`, `question`, `quiz`, `attempt`, `leaderboard`, `admin`, `security`, `docker`, `docs`, `ci`, `ui`, and `scripts`.

Individual fork commits do not need to be perfect when the PR title is suitable for squash merge.

## Required Checks

Run relevant checks locally when practical. Not every PR needs every command locally, but CI will enforce the required gates.

Always run for Java code changes:

```bash
mvn spotless:check
mvn clean test
```

Run for documentation changes:

```bash
mkdocs build --strict
```

Run for demo tooling changes:

```bash
python3 -m unittest discover scripts/demo/tests
```

Run for Javadoc/code documentation changes:

```bash
mvn -q javadoc:javadoc
```

Run for Docker/runtime changes:

```bash
docker compose config
docker compose build
```

Use `mvn spotless:apply` to apply Java formatting when `mvn spotless:check` reports formatting drift.

## Documentation Rules

Use English for docs and Javadocs. Update docs when behavior changes. Keep `CONTRIBUTING.md` as the main contributor workflow source and avoid duplicating full guidance across the docs site.

Do not document `QUIZZ_BASE_URL` as supported. The local base URL is derived from `QUIZZ_HTTP_PORT`.

Add or update ADRs for significant architectural decisions.

## Javadoc/Code Documentation Rules

Use English for code documentation. Keep public package and type documentation accurate, concise, and aligned with the code. Run `mvn -q javadoc:javadoc` for Javadoc or package documentation changes.

## UI/Design System Rules

Quizz uses server-rendered Thymeleaf templates and project-owned CSS/JavaScript. Do not add Bootstrap, Tailwind, React, Vue, or an npm application build pipeline.

Do not expose correct answers on the play page. Do not expose `originalAnswerOptionId` or correctness metadata on play views.

## Database Migration Rules

Use Flyway migrations for schema changes. Keep migrations focused, ordered, and compatible with existing PostgreSQL runtime assumptions. Do not edit already-applied migration files unless maintainers explicitly direct that correction.

## Security Rules

- Do not commit secrets.
- Do not store passwords in `.env`.
- Keep database password material in Docker secrets for Docker runtime.
- Do not expose password hashes or private user data in templates.
- Do not turn mutating actions into GET links.
- Keep POST + CSRF for mutating forms.
- Do not inject repositories into controllers.
- Respect command/query service boundaries.
- Use least privilege for GitHub Actions permissions.

## Review Expectations

Prefer small focused PRs. Describe the behavior change, verification performed, skipped checks, and any security or privacy impact. Reviewers should prioritize correctness, security boundaries, tests, and documentation alignment.

## Maintainer Merge Policy

Maintainers should use squash merge after required checks pass and review is complete. The squash commit title should keep the validated Conventional Commit PR title. CI is the final source of truth for required gates.
