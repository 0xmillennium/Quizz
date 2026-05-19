# Quizz Documentation

Quizz is a Spring Boot MVC quiz platform with server-rendered authoring, attempt-taking, scoring, leaderboard, and admin reporting workflows. The application uses PostgreSQL with Flyway migrations, Spring Security with CSRF protection, and Docker Compose for local runtime.

These docs are for developers and maintainers who need to run, understand, operate, or extend the project.

## Start Here

| Reader | Start with |
| --- | --- |
| New developer | [Getting Started](getting-started/index.md), then [Docker](getting-started/docker.md) |
| Architecture reader | [Architecture Overview](architecture/index.md), then [Package Boundaries](architecture/package-boundaries.md) |
| Operator or local runtime maintainer | [Operations](operations/index.md), then [Configuration](operations/configuration.md) and [Secrets](operations/secrets.md) |
| Maintainer looking for exact facts | [Reference](reference/index.md) |
| Decision history reader | [Decisions](decisions/index.md) |

## Important Commands

```bash
docker compose up --build
mvn clean test
python3 -m unittest discover scripts/demo/tests
mkdocs serve
mkdocs build --strict
```

## Main Sections

- [Getting Started](getting-started/index.md): task-oriented onboarding paths.
- [Architecture](architecture/index.md): system design, package boundaries, domain model, lifecycle rules.
- [Operations](operations/index.md): runtime configuration, secrets, Docker operation, troubleshooting.
- [Reference](reference/index.md): route, configuration, schema, script, testing, and glossary facts.
- [Decisions](decisions/index.md): accepted architectural decisions.
- [Contributing](contributing/documentation-style-guide.md): documentation and Javadoc standards.
