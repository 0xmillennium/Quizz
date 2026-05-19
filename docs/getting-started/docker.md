# Docker Setup

This guide brings up Quizz with Docker Compose, PostgreSQL, Flyway, and Docker secrets.

## Prerequisites

- Docker
- Docker Compose plugin
- OpenSSL

## Steps

1. Copy non-secret local configuration:

   ```bash
   cp .env.example .env
   ```

2. Review `.env`.

   Keep only non-secret local configuration in this file. Do not put database passwords, admin passwords, tokens, or API keys in `.env`.

3. Create the PostgreSQL Docker secret:

   ```bash
   mkdir -p docker/secrets
   umask 077
   openssl rand -base64 48 | tr -d '\n' > docker/secrets/postgres_password.txt
   chmod 600 docker/secrets/postgres_password.txt
   ```

4. Validate Compose interpolation:

   ```bash
   docker compose config
   ```

5. Start the runtime:

   ```bash
   docker compose up --build
   ```

6. Verify health:

   ```bash
   docker compose ps
   curl -fsS http://localhost:<QUIZZ_HTTP_PORT>/actuator/health
   ```

7. Bootstrap the admin account:

   ```bash
   scripts/bootstrap-admin.sh
   ```

8. Load demo fixtures:

   ```bash
   python3 scripts/demo/load-all-demo-catalog.py
   ```

9. Open the application:

   ```text
   http://localhost:<QUIZZ_HTTP_PORT>
   ```

## Configuration Rules

- `.env` is for non-secret local configuration only.
- The database password is read from `docker/secrets/postgres_password.txt` as a Docker secret.
- `QUIZZ_BASE_URL` is not supported. Local tooling derives the base URL from `QUIZZ_HTTP_PORT`.

## Troubleshooting

- Port conflict: see [Troubleshooting: port already allocated](../operations/troubleshooting.md#port-already-allocated).
- Missing `.env`: see [Troubleshooting: env file not found](../operations/troubleshooting.md#env-file-not-found).
- Missing Docker secret: see [Troubleshooting: docker-secret-missing](../operations/troubleshooting.md#docker-secret-missing).
- Stale volume: see [Troubleshooting: stale-docker-volume-after-schema-changes](../operations/troubleshooting.md#stale-docker-volume-after-schema-changes).
