# Code Documentation Standard

Quizz uses Javadoc and package documentation to describe contracts, invariants, boundaries, and non-obvious lifecycle behavior.

## Package Documentation

Use `package-info.java` for feature-level responsibility and boundary rules. Package docs should answer:

- What does this package own?
- Which packages or services collaborate with it?
- What must not cross this boundary?

## Class-level Javadocs

Use class-level Javadocs for:

- Aggregate roots and owned children.
- Service contracts.
- Controllers with route group responsibilities.
- Security and tooling adapters.
- Read-model repositories with privacy or query semantics.

## Service Contract Docs

Service interfaces should describe command/query responsibility, aggregate ownership, expected callers, and important invariants.

- Command services mutate state and enforce write invariants.
- Query services read and shape data.
- Controllers depend on service contracts, not repositories.

## Aggregate Invariant Docs

Entity and aggregate documentation should describe lifecycle and invariants, such as:

- Question ownership of answer options.
- Quiz ownership of pool memberships.
- Attempt ownership of immutable snapshots.
- Attempt allowance rights and cooldown semantics.

## Critical Method Docs

Add method Javadocs only when the method has non-trivial contract behavior:

- Starting or resuming attempts.
- Restart snapshot reuse.
- Autosave revision guards.
- Manual submit and time-expired submit.
- Publishing validation.
- Scoring from snapshots.

## Implementation Comments

Inline comments should explain why a non-obvious implementation choice exists. Examples include restart anti-question-fishing, stale autosave protection, and cooldown timing.

Do not add comments that repeat a method name, field name, or obvious code statement.

## Avoid Boilerplate

Do not write getter/setter Javadocs or record accessor documentation. Avoid generic comments that do not add contract or boundary information.

## Keep Docs Current

Do not describe future behavior as current behavior. When changing executable behavior, update Javadocs and docs in the same change so maintainers can trust them.
