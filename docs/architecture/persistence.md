# Persistence Architecture

PostgreSQL is the application database. Flyway migrations define the schema, and Hibernate validates the schema at startup.

## Schema Ownership

- Flyway migrations in `src/main/resources/db/migration` are the schema source.
- Hibernate uses `ddl-auto=validate`.
- `spring.jpa.open-in-view=false` keeps persistence access inside service boundaries.

## Key Design Choices

- Case-insensitive unique index on `users.lower(email)`.
- Case-insensitive unique index on `categories.lower(name)`.
- Explicit `QuizQuestion` join entity instead of `ManyToMany`.
- Snapshot tables for attempts, questions, and answer options.
- Partial unique index for one active attempt per user and quiz.
- Attempt allowance table for remaining attempts and cooldown.
- Lifecycle flags/statuses instead of hard delete for categories, questions, quizzes, and attempts.

## Snapshot Tables

`quiz_attempts`, `attempt_questions`, and `attempt_answer_options` store the quiz content and correctness used for scoring and reporting. This allows live question-bank changes without altering historical attempts.

## Validation Constraints

Database checks enforce status values, score bounds, lifecycle consistency, positive display order, and count totals. Application services still enforce business rules before persistence so users receive domain-level errors.
