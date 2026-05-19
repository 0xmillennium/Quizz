# Release and Delivery

## Release Model

Quizz releases are created from stable semantic version tags. Release tags are created from validated commits on the protected `master` branch. Normal development happens on side branches, then through pull requests targeting `master`, with squash merge as the normal merge strategy.

## Semantic Version Tags

Release tags use `vMAJOR.MINOR.PATCH` and must be annotated Git tags.

Examples:

```bash
v0.1.0
v1.2.3
```

Do not move a published semantic version tag. A tag identifies a released source revision and the container image built from that revision.

Release tags must point to commits reachable from `master`. The release workflow checks that the release ref is an annotated tag object and that the tag target commit is reachable from `master` before publishing, so lightweight tags and tags outside `master` history fail before any GHCR image push.

## Create a Release

Create release tags only after the target commit has passed the required checks on `master`.

```bash
git checkout master
git pull --ff-only
git tag -a v0.1.0 -m "v0.1.0"
git push origin v0.1.0
```

Pushing the tag starts the release workflow.

Use the [First Release Runbook](first-release-runbook.md) for the initial `v0.1.0` release and post-publish artifact verification.

## Release Validation

The release workflow validates the tagged source before publishing:

- Tag format and reachability from `master`.
- Spotless formatting.
- Maven tests.
- Javadocs.
- Python demo tooling tests.
- Critical static audits for templates, documentation, branch governance wording, and sensitive data exposure.
- MkDocs strict build.
- Docker Compose configuration.
- Docker image build.
- Docker runtime health smoke against `/actuator/health`.

The workflow does not publish images from pull requests or ordinary pushes to `master`.

## GHCR Image Publishing

Release images are published to:

```text
ghcr.io/0xmillennium/quizz
```

For tag `v1.2.3`, the workflow publishes:

```text
ghcr.io/0xmillennium/quizz:1.2.3
ghcr.io/0xmillennium/quizz:1.2
ghcr.io/0xmillennium/quizz:1
ghcr.io/0xmillennium/quizz:latest
```

The `latest` tag means the latest stable release tag, not the latest commit on `master`.

## Image Digest

An image digest identifies the exact immutable image produced by the release build. Prefer the digest from the GitHub Release notes when deploying or auditing an exact release artifact.

## SBOM, Provenance, and Attestation

The release image build requests SBOM and provenance metadata from Docker Buildx. The release workflow also creates a GitHub artifact attestation for the pushed container image. These records help consumers answer what was built, from which source revision, and by which workflow identity.

Verify the container attestation with GitHub CLI:

```bash
gh attestation verify oci://ghcr.io/0xmillennium/quizz:1.2.3 -R 0xmillennium/quizz
```

Use the released version tag or digest that appears in the GitHub Release notes.

After a release, verify the GHCR image can be pulled, inspect the digest, verify the GitHub Release, and confirm the GHCR package visibility and repository link. The first release runbook contains copy-pasteable verification commands.

## GitHub Pages Deployment

Documentation is deployed to GitHub Pages from `master` through the Pages workflow. Pull requests validate documentation but do not deploy it.

Repository Pages source must be set to GitHub Actions. The workflow builds MkDocs with `mkdocs build --strict`, uploads the generated `site` directory as the Pages artifact, and deploys with the official GitHub Pages actions.

Manual Pages runs are allowed only when the selected ref is `master`. The workflow has a guard job that fails any run whose `GITHUB_REF` is not `refs/heads/master`, so manual runs from side branches fail before build, upload, or deployment.

Pages permissions are scoped by job: the build job has read access, and only the deploy job has Pages and OIDC write access.

## Required Repository Settings

Recommended repository settings:

- Pages source: GitHub Actions.
- Package visibility: public, if the container image is intended to be public.
- Package linked to this repository.
- Actions access configured so repository workflows can publish the package.
- Branch protection enabled for `master`.
- Tag protection or repository ruleset enabled for `v*.*.*`.
- Tag creation, update, and deletion restricted to maintainers.

Use the [Repository Settings Checklist](repository-settings-checklist.md) before creating release tags.

## Rollback Guidance

Do not move published semantic version tags to roll back. Deploy a previous known-good released version tag when an immediate rollback is needed. Cut a new patch release for fixes so the release history stays append-only and auditable.
