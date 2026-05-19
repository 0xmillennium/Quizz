# ADR-0004: Autosave, Resume, and Auto-submit

## Status

Accepted

## Context

Quiz-taking sessions can be interrupted by navigation, network failure, or browser refresh. Time limits must still be enforced server-side.

## Decision

Save each answer change with autosave, allow users to resume active attempts, and auto-submit saved answers when the server-side expiry has passed.

## Consequences

- Users can recover from refreshes or brief interruptions.
- The server remains authoritative for expiry.
- After-expiry submit ignores incoming payloads and uses saved answers.
- The lifecycle is more complex because command services must reconcile overdue attempts at user-facing boundaries.

## Alternatives considered

- Submit-only answers: rejected because a lost browser session could lose all progress.
- Client-only timer enforcement: rejected because expiry must be enforced by the server.
