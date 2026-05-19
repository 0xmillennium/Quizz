# ADR-0003: Immutable Attempt Snapshots

## Status

Accepted

## Context

Questions, answer options, and quizzes can change after users take attempts. Results and admin reports must reflect what the user actually saw, not the current live question-bank state.

## Decision

Copy selected quiz questions and answer options into attempt snapshot tables when an attempt starts. Score and report from the snapshot.

## Consequences

- Scoring is stable even if live questions or options are edited later.
- Result views and admin reports can show historical attempt content accurately.
- Snapshot correctness is available for scoring and reporting while play DTOs can exclude it.
- Storage usage increases because attempt content is duplicated.

## Alternatives considered

- Join result views to live question-bank tables: rejected because edits would alter historical meaning.
- Store only selected live ids: rejected because text, option order, and correctness need historical stability.
