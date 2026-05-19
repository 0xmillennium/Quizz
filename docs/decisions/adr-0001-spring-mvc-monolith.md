# ADR-0001: Spring MVC Monolith

## Status

Accepted

## Context

Quizz needs authenticated quiz-taking, admin authoring, leaderboard, reporting, and local operations without a separate frontend build pipeline. The application has strong server-side lifecycle rules and form-heavy admin workflows.

## Decision

Build Quizz as a server-rendered Spring MVC monolith with Thymeleaf templates. Keep controllers, services, repositories, and templates in one deployable Spring Boot application. Do not introduce a SPA or REST-first frontend/backend split.

## Consequences

- Deployment remains simple: one application jar plus PostgreSQL.
- MVC forms can rely on Spring Security CSRF protection directly.
- The project avoids npm, frontend build tooling, and client-side route ownership.
- UI and backend changes are coupled inside one application, which is acceptable for this codebase.

## Alternatives considered

- SPA with REST API: rejected because it adds build, routing, auth, and deployment complexity that the current product does not need.
- Separate backend and frontend services: rejected because local operations and feature velocity benefit from one deployable unit.
