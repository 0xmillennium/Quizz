# ADR-0005: Randomized Quiz Pools and Attempt Rights

## Status

Accepted

## Context

Quizzes should support a larger question pool than the number shown in one attempt. Retakes should be limited so users cannot repeatedly sample the pool without cost.

## Decision

Model quizzes as question pools. Fresh attempts randomly sample `questionCount` questions, randomize question order, randomize option order, consume attempt rights, and enter cooldown after rights are exhausted. Restart copies the same snapshot instead of sampling again.

## Consequences

- Attempts are less predictable.
- Restart cannot be used to fish for new questions.
- Resume keeps the same attempt snapshot and does not consume rights.
- The application needs explicit allowance and cooldown state.

## Alternatives considered

- Fixed question order for all attempts: rejected because it makes repeated attempts easier to memorize.
- Restart with a fresh sample: rejected because it undermines attempt rights.
