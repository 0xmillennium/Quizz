# ADR-0006: Secrets and Local Configuration Boundary

## Status

Accepted

## Context

Local runtime needs convenient configuration while keeping credentials out of committed files and shell history.

## Decision

Use `.env` only for non-secret local configuration, Docker secrets for the database password, and an interactive admin password prompt for bootstrap. Do not support `QUIZZ_BASE_URL`; derive local tooling URLs from `QUIZZ_HTTP_PORT`.

## Consequences

- Local setup has a clear boundary between configuration and secrets.
- Scripts can validate and reject secret-like `.env` keys.
- Admin bootstrap avoids passing the password as a command-line argument.
- Operators must create the Docker secret file before starting the runtime.

## Alternatives considered

- Store all local values in `.env`: rejected because secrets would be too easy to commit or print.
- Support a configurable base URL: rejected because local tooling is intentionally scoped to the configured local HTTP port.
