# Quizz

Quizz is a Spring Boot MVC quiz platform for server-rendered quiz authoring, taking, scoring, leaderboard, and admin reporting workflows. It uses PostgreSQL with Flyway-managed schema, Docker Compose for local runtime, and MkDocs for maintainer documentation.

## Feature Summary

- Randomized quiz question pools with shuffled answer options per attempt.
- Attempt rights, cooldown windows, autosave, resume, restart, and auto-submit after expiry.
- Admin category, question, and quiz management.
- Public leaderboard from completed submitted attempts.
- Admin dashboard and attempt-result reporting from stored snapshots.
- Dockerized local runtime with PostgreSQL, Flyway, and Docker secrets.

## Tech Stack

- Java 25
- Spring Boot 4.x
- Spring MVC + Thymeleaf
- Spring Security
- PostgreSQL
- Flyway
- Docker Compose
- MkDocs

## Quick Start With Docker

1. Copy local non-secret configuration:

   ```bash
   cp .env.example .env
   ```

2. Create the PostgreSQL Docker secret:

   ```bash
   mkdir -p docker/secrets
   umask 077
   openssl rand -base64 48 | tr -d '\n' > docker/secrets/postgres_password.txt
   chmod 600 docker/secrets/postgres_password.txt
   ```

3. Start the runtime:

   ```bash
   docker compose up --build
   ```

4. Bootstrap an admin account:

   ```bash
   scripts/bootstrap-admin.sh
   ```

5. Load demo catalog fixtures:

   ```bash
   python3 scripts/demo/load-all-demo-catalog.py
   ```

6. Open `http://localhost:<QUIZZ_HTTP_PORT>` using the port configured in `.env`.

Full setup details live in [Getting Started with Docker](docs/getting-started/docker.md).

## Test Commands

```bash
mvn clean test
python3 -m unittest discover scripts/demo/tests
mkdocs build --strict
```

## Documentation

- [Documentation home](docs/index.md)
- [Getting Started](docs/getting-started/index.md)
- [Docker onboarding](docs/getting-started/docker.md)
- [Admin bootstrap](docs/getting-started/admin-bootstrap.md)
- [Demo fixtures](docs/getting-started/demo-fixtures.md)
- [Architecture](docs/architecture/index.md)
- [Operations](docs/operations/index.md)
- [Reference](docs/reference/index.md)
- [Decisions](docs/decisions/index.md)
- [Contributing](docs/contributing/documentation-style-guide.md)

## Security and Configuration

`.env` is only for non-secret local configuration. Database password material belongs in `docker/secrets/postgres_password.txt`, and the admin password is entered interactively during bootstrap. `QUIZZ_BASE_URL` is not a supported configuration key; tooling derives the local base URL from `QUIZZ_HTTP_PORT`.
