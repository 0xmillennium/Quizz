# Local Setup

Quizz is built for Java 25, Maven, and PostgreSQL. The dev profile expects a local PostgreSQL database.

## Requirements

- Java 25
- Maven
- PostgreSQL

## Dev Database

`src/main/resources/application-dev.yml` expects:

- database: `quizz`
- username: `quizz`
- password: `quizz`
- JDBC URL: `jdbc:postgresql://localhost:5432/quizz`

Example PostgreSQL setup commands, which may vary by OS and local PostgreSQL installation:

```bash
createuser quizz
createdb quizz
psql -d quizz -c "ALTER USER quizz WITH PASSWORD 'quizz';"
```

If your local PostgreSQL user already exists, adjust the commands accordingly.

## Flyway

Flyway is enabled and runs migrations from `src/main/resources/db/migration` on application startup. Hibernate uses `ddl-auto=validate`, so the app validates the schema but does not create or update tables automatically.

## Run Tests

```bash
mvn clean test
```

The current test suite is designed to run without Docker, Testcontainers, or an external PostgreSQL instance.

## Run The App

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The default profile in `application.yml` is also `dev`, but passing the profile explicitly is useful when switching environments.

## Troubleshooting

- If startup fails with a database connection error, verify PostgreSQL is running, the `quizz` database exists, and the configured credentials match `application-dev.yml`.
- If startup fails during Flyway, check whether the database already has partially applied or edited migrations.
- If Maven compilation fails with a release error, verify `java -version` and `mvn -version` both point to Java 25.
- If Hibernate validation fails, compare entity mappings with the Flyway migrations. Runtime schema generation is intentionally disabled.
