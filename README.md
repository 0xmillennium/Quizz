# Quizz

Quizz is a Spring Boot MVC quiz application with public registration/login, admin-managed quiz content, timed quiz attempts, result history, leaderboard views, and admin reporting.

## Tech Stack

- Java 25
- Spring Boot 4.x
- Maven
- Spring MVC and Thymeleaf
- Spring Data JPA and Spring JDBC
- Spring Security
- Bean Validation
- Flyway
- PostgreSQL

## Main Features

- Users can register, log in, browse published quizzes, complete timed attempts, view scores, see result history, and use the leaderboard.
- Admins can manage categories, questions, and quizzes, publish or archive quizzes, and review submitted results.
- Attempts store quiz, question, and answer snapshots so historical results remain stable after content changes.
- Leaderboard and admin reporting are read models backed by JDBC queries over attempt snapshots.

## Tests

Run the full test suite:

```bash
mvn clean test
```

## Local Run

The default profile is `dev`. The dev configuration expects PostgreSQL at `localhost:5432` with database, user, and password all named `quizz`.

Example local commands:

```bash
createdb quizz
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Flyway runs database migrations on startup. Hibernate is configured with `ddl-auto=validate`, so schema changes must come from migrations rather than runtime DDL.

More setup detail is in [docs/LOCAL_SETUP.md](docs/LOCAL_SETUP.md).

## Docker Quick Start

Docker is an additional local runtime option. Create the local secret file first:

```bash
mkdir -p docker/secrets
openssl rand -base64 48 | tr -d '\n' > docker/secrets/postgres_password.txt
chmod 600 docker/secrets/postgres_password.txt
```

Then start the application and PostgreSQL:

```bash
docker compose up --build
```

Bootstrap an admin account:

```bash
./scripts/bootstrap-admin.sh
```

Application URL: <http://localhost:8081>

Health URL: <http://localhost:8081/actuator/health>

PostgreSQL is intentionally not exposed to the host; it is reachable only by the Quizz container on the internal backend network. Full Docker workflow details are in [docs/DOCKER.md](docs/DOCKER.md), and admin bootstrap details are in [docs/ADMIN_BOOTSTRAP.md](docs/ADMIN_BOOTSTRAP.md).

## Profiles And Configuration

- `spring.application.name=quizz`
- `spring.jpa.hibernate.ddl-auto=validate`
- `spring.jpa.open-in-view=false`
- `spring.flyway.enabled=true`
- `application-dev.yml` uses the `quizz` PostgreSQL database naming.
- `application-test.yml` keeps strict JPA and Flyway settings, but the current tests avoid requiring an external database.

## Route Overview

Public:

- `GET /`
- `GET /login`
- `POST /login`
- `GET /register`
- `POST /register`
- `POST /logout`

Authenticated user:

- `GET /quizzes`
- `GET /quizzes/{id}`
- `POST /attempts/start`
- `GET /attempts/{attemptId}`
- `POST /attempts/{attemptId}/submit`
- `GET /attempts/{attemptId}/result`
- `GET /attempts/history`
- `GET /attempts/{attemptId}/chart-data`
- `GET /leaderboard`

Admin:

- `GET /admin`
- `GET /admin/dashboard`
- `GET /admin/categories`
- `GET /admin/categories/new`
- `POST /admin/categories`
- `GET /admin/categories/{id}/edit`
- `POST /admin/categories/{id}`
- `POST /admin/categories/{id}/activate`
- `POST /admin/categories/{id}/deactivate`
- `GET /admin/questions`
- `GET /admin/questions/new`
- `POST /admin/questions`
- `GET /admin/questions/{id}/edit`
- `POST /admin/questions/{id}`
- `POST /admin/questions/{id}/delete`
- `POST /admin/questions/{id}/restore`
- `GET /admin/quizzes`
- `GET /admin/quizzes/new`
- `POST /admin/quizzes`
- `GET /admin/quizzes/{id}`
- `GET /admin/quizzes/{id}/edit`
- `POST /admin/quizzes/{id}`
- `POST /admin/quizzes/{id}/publish`
- `POST /admin/quizzes/{id}/archive`
- `GET /admin/results`
- `GET /admin/results/{attemptId}`

## Documentation

- [Local setup](docs/LOCAL_SETUP.md)
- [Docker runtime](docs/DOCKER.md)
- [Admin bootstrap](docs/ADMIN_BOOTSTRAP.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Final requirements checklist](docs/FINAL_REQUIREMENTS_CHECKLIST.md)
- [Phase summary](docs/PHASE_SUMMARY.md)
