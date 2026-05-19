# Testing Reference

## Commands

```bash
mvn spotless:check
mvn clean test
mvn -q javadoc:javadoc
python3 -m unittest discover scripts/demo/tests
mkdocs build --strict
```

Docker/runtime changes should also be checked with:

```bash
docker compose config
docker compose build
docker compose up --build --wait
QUIZZ_HTTP_PORT="$(awk -F= '/^QUIZZ_HTTP_PORT=/{print $2; exit}' .env | tr -d '\r' | sed 's/^"//; s/"$//')"
curl -fsS "http://localhost:${QUIZZ_HTTP_PORT}/actuator/health"
docker compose down -v
```

## Test Categories

| Category | Coverage |
| --- | --- |
| Service/unit tests | Domain services, scoring, validation, mappers, CLI helpers. |
| MVC/controller tests | Route handling, model/view behavior, redirects, form validation. |
| Repository SQL/read-model tests | Leaderboard and admin JDBC query models. |
| Architecture/layering/security tests | Package dependency rules, layering rules, security boundary rules, route security. |
| Python demo tooling tests | `.env` loading, CSRF parsing, fixture parsing, form helpers. |

## Release and Pages Validation

The release workflow repeats the local quality gates, verifies the release tag is an annotated tag object, verifies the tag target commit is reachable from `master`, runs critical static audits, builds MkDocs with strict mode, validates Compose, builds the Docker image, starts the runtime, and checks `/actuator/health` before publishing a GHCR image.

The Pages workflow first guards the deployment ref and allows only `refs/heads/master`. Manual Pages runs are allowed only when the selected ref is `master`; non-master manual runs fail before build, upload, or deployment. Pull requests validate documentation but do not deploy it.

After the ref guard passes, the Pages workflow installs the documentation dependencies, runs the documentation static audit, builds MkDocs with strict mode, uploads the generated `site` directory, and deploys through GitHub Pages. Deployment happens only after the strict build succeeds.

Container attestations for release images can be checked with:

```bash
gh attestation verify oci://ghcr.io/0xmillennium/quizz:1.2.3 -R 0xmillennium/quizz
```

See [Release and Delivery](../operations/release-and-delivery.md) for the release model, image tags, digest handling, and repository settings.

For the first release, use the [First Release Runbook](../operations/first-release-runbook.md). It includes the `v0.1.0` pre-tag checks, tag command, GHCR pull checks, digest inspection, attestation verification, GitHub Release verification, and Pages verification.

## Not Covered

- Full browser visual QA.
- Production deployment.
- External managed database operation.
- Third-party infrastructure behavior.
