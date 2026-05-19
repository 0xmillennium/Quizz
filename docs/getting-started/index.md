# Getting Started

Use Docker as the default onboarding path. It runs the Spring Boot application and PostgreSQL with the same local runtime shape used by the project documentation.

## Recommended Path

1. Complete [Docker setup](docker.md).
2. Run the application until the health endpoint is healthy.
3. Create or update the admin account with [Admin Bootstrap](admin-bootstrap.md).
4. Load catalog data with [Demo Fixtures](demo-fixtures.md).
5. Use [Local Development](local-development.md) when you need direct Java and PostgreSQL workflows outside Compose.

## Choosing a Setup

| Setup | Use when |
| --- | --- |
| Docker | You want the fastest reliable local runtime with Docker secrets and PostgreSQL managed by Compose. |
| Local development | You are running Java directly with Maven and managing PostgreSQL yourself. |

Docker setup does not require a default admin account. The admin bootstrap script creates the admin user after the application and database are healthy.
