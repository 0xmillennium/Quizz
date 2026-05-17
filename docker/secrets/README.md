# Local Docker Secrets

This directory is for local Docker secret files used by Docker Compose.

Real secret files are ignored by git. Create `postgres_password.txt` manually before starting the Docker runtime:

```bash
mkdir -p docker/secrets
openssl rand -base64 48 | tr -d '\n' > docker/secrets/postgres_password.txt
chmod 600 docker/secrets/postgres_password.txt
```

Never commit real secrets.
