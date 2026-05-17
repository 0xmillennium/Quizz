# Phase Summary

## Phase 1 Foundation

Established the Spring Boot MVC project, Java 25 Maven build, PostgreSQL/Flyway baseline, strict JPA validation, common entity support, exception handling, and MVC configuration.

## Phase 2 User/Auth/Security

Added user registration, login, roles, BCrypt password storage, `CustomUserDetails`, security configuration, CSRF-protected forms, current-user access through `CurrentUserProvider`, and role-based route protection.

## Phase 3 Category

Added category entity, repository, service layer, mapper, validation, and admin category screens for create, update, activate, and deactivate flows.

## Phase 4 Question Bank

Added question and answer option entities, validation for option counts and one correct answer, admin question create/update/archive/restore flows, and category-based question ownership.

## Phase 5 Quiz Definition

Added quiz and quiz-question entities, draft/publish/archive lifecycle, category-based quiz composition, ordered question selection, and public published quiz browsing.

## Phase 6 Attempt/Scoring/Results

Added attempt snapshots, timed attempt lifecycle, answer submission, server-side expiry checks, scoring from snapshots, result pages, history, and result chart data.

## Phase 7 Leaderboard

Added read-only leaderboard service and JDBC repository using completed attempt snapshots, with overall, quiz, and category scopes.

## Phase 8 Admin Reporting

Added admin dashboard and result reporting read models using JDBC queries over attempt snapshots, including detail pages that show stored question and option snapshots.

## Phase 9 Hardening

Added executable architecture, layering, security boundary, and route security matrix tests. Polished navigation consistency, documented local setup/admin bootstrap/architecture/final requirements, and audited forbidden naming, dependency, security, template, static asset, and configuration patterns.
