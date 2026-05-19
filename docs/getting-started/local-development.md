# Local Development

Local development runs Java and Maven directly while you manage PostgreSQL outside Docker Compose.

## Prerequisites

- Java 25
- Maven
- PostgreSQL

## Spring Profiles

| Profile | Purpose |
| --- | --- |
| `dev` | Default local profile for direct Maven runs. Uses a local PostgreSQL connection. |
| `test` | Test profile used by automated tests. |
| `docker` | Runtime profile used by Docker Compose and Docker secrets. |

## Database Expectations

The `dev` profile expects PostgreSQL to be reachable with the datasource configured in `src/main/resources/application-dev.yml`. Flyway runs migrations at startup, and Hibernate uses `ddl-auto=validate`, so the schema must match the migrations.

## Useful Commands

```bash
mvn clean test
mvn spring-boot:run
```

## What Local Development Does Not Replace

Direct Maven runs do not exercise the Docker runtime topology, Docker secrets, internal backend network, read-only app container filesystem, or Compose healthcheck ordering. Use [Docker Setup](docker.md) when validating local runtime behavior.
