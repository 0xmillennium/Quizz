# Secrets

Secrets are values that grant access or should not be committed, printed, or stored in `.env`.

## Secret and Non-secret Boundary

| Type | Examples | Location |
| --- | --- | --- |
| Secret | Database password, admin password, password hashes, tokens | Docker secrets or interactive prompt |
| Non-secret local configuration | HTTP port, default admin email, default admin full name, database name, database username | `.env` |

## PostgreSQL Docker Secret

The local database password is stored in:

```text
docker/secrets/postgres_password.txt
```

Generate it locally:

```bash
mkdir -p docker/secrets
umask 077
openssl rand -base64 48 | tr -d '\n' > docker/secrets/postgres_password.txt
chmod 600 docker/secrets/postgres_password.txt
```

Do not commit this file. The `docker/secrets/.gitkeep` file only preserves the directory.

## Admin Password

The admin bootstrap script prompts interactively for the admin password. Do not pass it as an argument, store it in `.env`, or write it to disk.

## Forbidden `.env` Keys

The local env loaders reject these keys because they belong outside `.env`:

- `POSTGRES_PASSWORD`
- `SPRING_DATASOURCE_PASSWORD`
- `ADMIN_PASSWORD`
- `QUIZZ_ADMIN_PASSWORD`

## Local Secret Rotation

For a disposable local database:

1. Stop the runtime:

   ```bash
   docker compose stop
   ```

2. Replace `docker/secrets/postgres_password.txt` with a newly generated value.
3. Recreate the database volume if PostgreSQL was initialized with the old password.
4. Start the runtime and bootstrap the admin account again.

For an existing local volume, PostgreSQL keeps the password from initialization. Recreating the secret file alone does not rewrite the existing database role password.
