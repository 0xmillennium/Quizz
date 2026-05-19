# Testing Reference

## Commands

```bash
mvn clean test
python3 -m unittest discover scripts/demo/tests
mvn javadoc:javadoc
mkdocs build --strict
```

## Test Categories

| Category | Coverage |
| --- | --- |
| Service/unit tests | Domain services, scoring, validation, mappers, CLI helpers. |
| MVC/controller tests | Route handling, model/view behavior, redirects, form validation. |
| Repository SQL/read-model tests | Leaderboard and admin JDBC query models. |
| Architecture/layering/security tests | Package dependency rules, layering rules, security boundary rules, route security. |
| Python demo tooling tests | `.env` loading, CSRF parsing, fixture parsing, form helpers. |

## Not Covered

- Full browser visual QA.
- Production deployment.
- External managed database operation.
- Third-party infrastructure behavior.
