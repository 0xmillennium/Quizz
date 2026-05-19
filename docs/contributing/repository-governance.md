# Repository governance

## Default branch

- The default branch is `master`.
- Direct commits to `master` are not part of the contribution model.

## Contribution flow

1. Create a side branch.
2. Commit focused changes.
3. Open a pull request targeting `master`.
4. Pass required checks.
5. Maintainers squash and merge.

## Required branch protection for `master`

Recommended GitHub branch protection settings:

- Require a pull request before merging.
- Require at least one approval if maintainers choose to enforce review.
- Require status checks before merging.
- Require branches to be up to date before merging.
- Require conversation resolution.
- Require linear history.
- Restrict direct pushes to `master`.
- Disable force pushes.
- Disable branch deletion.
- Use squash merge as the normal merge strategy.

The full maintainer checklist is in [Repository Settings Checklist](../operations/repository-settings-checklist.md).

## Required status checks

Use stable required check names from the workflow and job names:

- CI / Java, Python, Javadocs, and static audits
- CI / PR title
- Docs / MkDocs and documentation audits
- Docker Check / Compose config, image build, and health smoke

The Docker Check workflow validates Compose configuration, builds the images, starts the runtime with Compose health waiting, and curls the application health endpoint.

## CODEOWNERS

The repository uses `.github/CODEOWNERS` to require review from `@0xmillennium` for sensitive paths. Covered areas include repository automation, workflows, dependency governance, release configuration, Docker runtime files, documentation navigation, database migrations, security/authentication/attempt code, architecture decisions, release documentation, and `SECURITY.md`.

CODEOWNERS review should be required for workflow changes under `.github/workflows/`, because those files control CI, release publishing, package permissions, attestation, and Pages deployment.

GitHub Private Vulnerability Reporting must remain enabled before public launch and while accepting public security reports. Sensitive vulnerability details must not use public issues as a fallback reporting path.

## Release and tag governance

Release tags should point to validated commits on `master`. The release workflow verifies that the release ref is an annotated tag object and that the tagged commit is reachable from `origin/master` before GHCR publishing can start.

Recommended tag protection or repository ruleset settings:

- Protect tags matching `v*.*.*`.
- Create release tags as annotated Git tags.
- Restrict release tag creation to maintainers.
- Restrict release tag updates and deletion to maintainers.
- Treat published release tags as immutable.
- Require the release validation job to pass before considering a release complete.

Release operators should follow the [First Release Runbook](../operations/first-release-runbook.md) for the first `v0.1.0` release.

## GitHub Pages governance

Repository Pages source must be GitHub Actions. Pull requests validate documentation, but only the Pages workflow deploys documentation from `master`. Manual Pages runs are allowed only when the selected ref is `master`; the workflow guard fails non-master refs before build, upload, or deployment.

## GHCR package governance

The GHCR package should be linked to this repository. If the project is intended to publish public images, set the package visibility to public and ensure repository Actions access permits the release workflow to publish with the built-in workflow token.

Workflow actions are pinned to reviewed full commit SHAs. Updates to those pins should go through the normal side branch, pull request, CODEOWNERS review, and required-check process.

## Merge strategy

- Squash merge is the default.
- PR title must follow Conventional Commits.
- The squash commit title should keep the validated PR title.

## Release note

- Release tags are created from validated commits on `master`.
- Container images are published only from semantic version release tags.
- GitHub Pages deployment is handled by the Pages workflow and does not run for pull requests.
