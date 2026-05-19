# ADR-0007: Server-rendered UI Design System

## Status

Accepted

## Context

Quizz needs a polished admin and user UI without adding frontend framework complexity. The application already uses Spring MVC and Thymeleaf.

## Decision

Use Thymeleaf templates, shared layout fragments, modular CSS, design tokens, and minimal vanilla JavaScript. Do not add a frontend framework or npm build pipeline.

## Consequences

- UI work remains inside the Spring Boot application.
- Styling is organized by tokens, layout, components, forms, tables, admin, and quiz surfaces.
- JavaScript stays feature-specific for forms, autosave, timer, charts, and leaderboard behavior.
- Advanced client-side interactivity is bounded by the server-rendered model.

## Alternatives considered

- Frontend framework: rejected because current workflows are form-centered and server-rendered.
- Generated static UI bundle: rejected because it introduces another build and deployment artifact.
