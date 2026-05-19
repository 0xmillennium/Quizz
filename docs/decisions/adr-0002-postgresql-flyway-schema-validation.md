# ADR-0002: PostgreSQL, Flyway, and Schema Validation

## Status

Accepted

## Context

The application stores account data, quiz definitions, attempt snapshots, attempt rights, and reporting data. The schema needs explicit constraints for lifecycle and scoring correctness.

## Decision

Use PostgreSQL as the database, Flyway migrations as the schema source, and Hibernate `ddl-auto=validate` at runtime.

## Consequences

- Schema changes are explicit and reviewable.
- Database constraints protect important invariants alongside application services.
- Hibernate validates mapping compatibility instead of generating schema changes.
- Local stale volumes can surface validation failures and must be recreated when disposable data is incompatible.

## Alternatives considered

- Hibernate auto-DDL: rejected because it can drift from reviewed schema intent.
- In-memory-only development database: rejected because PostgreSQL-specific constraints and indexes matter to the application.
