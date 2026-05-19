# Architecture Overview

Quizz is a server-rendered Spring MVC monolith. The application keeps feature behavior in package-oriented modules and uses service contracts between MVC controllers, domain aggregates, repositories, and read models.

## Architectural Shape

- Spring MVC controllers render Thymeleaf views and handle form binding.
- Command services own mutations and aggregate invariants.
- Query services prepare read data and keep controllers away from repositories.
- Spring Data JPA manages aggregate persistence.
- Spring JDBC powers leaderboard and admin reporting read models.
- PostgreSQL schema is owned by Flyway migrations.
- Docker Compose provides the local application and database runtime.

## Intentional Non-goals

- No SPA.
- No frontend framework.
- No npm build pipeline.
- No REST-first split between frontend and backend.
- No Hibernate-generated schema drift.

The result is a compact monolith with strong server-side lifecycle rules and low frontend operational complexity.
