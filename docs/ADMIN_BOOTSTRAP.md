# Admin Bootstrap

Public registration creates `USER` accounts only. Quizz does not include an admin creation UI, public admin registration, seed data, a `DevDataInitializer`, or a default admin account.

The recommended admin creation path is the Docker bootstrap script:

```bash
cp .env.example .env
chmod +x scripts/bootstrap-admin.sh
./scripts/bootstrap-admin.sh
```

Review `.env` before running the script. It is the single source for non-secret local bootstrap configuration. Do not put passwords, tokens, API keys, or database secrets in `.env`; the database password remains a Docker Compose secret.

The script reads from `.env`:

- admin email from `QUIZZ_DEFAULT_ADMIN_EMAIL`
- admin full name from `QUIZZ_DEFAULT_ADMIN_FULL_NAME`

The script prompts only for password and confirmation, read hidden from the terminal.

The plaintext password is not written to disk and is not passed as a command-line argument. The script sends it through stdin to the Quizz Docker image:

```bash
printf '%s' 'StrongPassword123!' | docker compose run --rm --no-deps quizz hash-password
```

The `hash-password` mode uses the same BCrypt encoder as the application and writes only the BCrypt hash to stdout. Validation errors are written to stderr.

After generating the hash, the script executes the SQL in `scripts/sql/upsert-admin.sql` through `docker compose exec -T postgres psql`. PostgreSQL does not need a host port; the script works with the private Compose network.

`POSTGRES_DB` and `POSTGRES_USER` come from `.env`. The PostgreSQL password does not; it stays in Docker Compose secrets.

The SQL is idempotent. It matches an existing user with `lower(email) = lower(:'admin_email')`, updates that user to `ADMIN` and `enabled = true`, or inserts a new admin user when no matching email exists.

## Direct Hash Generation

For troubleshooting, a BCrypt hash can be generated directly from the Compose image:

```bash
printf '%s' 'StrongPassword123!' | docker compose run --rm --no-deps quizz hash-password
```

After publishing an image to GHCR, the same CLI mode can be used without local Maven or a local JDK:

```bash
printf '%s' 'StrongPassword123!' | docker run --rm -i ghcr.io/<owner>/quizz:<tag> hash-password
```

Do not store real passwords or generated hashes in application config, Docker secrets, `.env` files, docs, or committed files.

## Advanced Troubleshooting

Manual SQL should only be used when debugging the bootstrap flow. Prefer `./scripts/bootstrap-admin.sh` for normal admin creation.

If manual execution is necessary, generate a hash through the image CLI, keep it only in memory for the current shell session, and pipe `scripts/sql/upsert-admin.sql` into `psql` inside the Postgres container with `-v admin_email=...`, `-v admin_full_name=...`, and `-v password_hash=...`.
