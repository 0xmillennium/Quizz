# Repository Settings Checklist

This checklist captures repository settings that maintainers must apply in GitHub before relying on public release automation.

## Default Branch

- Set the default branch to `master`.
- Keep `master` protected.
- Do not rename the default branch.
- Direct commits to `master` are not part of the contribution model.

## Branch Protection for `master`

Require these settings for `master`:

- Require a pull request before merging.
- Require approvals before merge.
- Require review from CODEOWNERS.
- Dismiss stale approvals when new commits are pushed.
- Require conversation resolution before merging.
- Require status checks before merging.
- Require branches to be up to date before merging.
- Require linear history.
- Restrict direct pushes to `master`.
- Disable force pushes.
- Disable branch deletion.

Required status checks should use the current workflow and job names:

- CI / Java, Python, Javadocs, and static audits
- CI / PR title
- Docs / MkDocs and documentation audits
- Docker Check / Compose config, image build, and health smoke

## Merge Strategy

Use squash merge as the normal merge strategy:

- Enable squash merge.
- Disable merge commits.
- Disable rebase merge when enforcing squash-only history.
- Automatically delete head branches after merge if maintainers want cleanup.
- Require pull request titles to follow Conventional Commits.
- Preserve the validated pull request title as the squash commit title.

## Tag Protection and Release Rules

Protect release tags with a tag ruleset or tag protection rule:

- Protect tags matching `v*.*.*`.
- Create release tags as annotated Git tags.
- Restrict creation of matching tags to maintainers.
- Restrict updates and deletions of matching tags.
- Require release tags to point to commits reachable from protected `master`.
- Do not move published semantic version tags.

The release workflow also verifies that a release tag commit is reachable from `origin/master` before GHCR publishing can start. It rejects lightweight release tags by requiring the release ref to be an annotated tag object.

## GitHub Pages

Configure Pages this way:

- Set Pages source to GitHub Actions.
- Deploy documentation through `.github/workflows/pages.yml`.
- Deploy only from `master`.
- Allow manual Pages runs only when the selected ref is `master`; the workflow guard fails non-master refs before build, upload, or deployment.
- Validate documentation on pull requests without deploying from pull requests.

## GHCR Package Settings

After the first image publish, verify the package settings:

- Package exists as `ghcr.io/0xmillennium/quizz`.
- Package is linked to this repository.
- Package visibility is public if public image distribution is intended.
- Repository Actions access allows this repository to publish with `GITHUB_TOKEN`.
- Package description and source link match this repository when GitHub exposes those fields.
- The `latest` tag points only to the latest stable release.

## Security Settings

Required GitHub security settings:

- Enable Dependabot alerts.
- Enable Dependabot security updates when available.
- Enable secret scanning when available.
- Enable push protection when available.
- Enable GitHub Private Vulnerability Reporting.
- Use GitHub Private Vulnerability Reporting as the required private reporting channel.
- Do not use public issues as a fallback for sensitive vulnerability details.
- Keep default Actions permissions restricted.
- Use the built-in workflow token for GHCR publishing instead of long-lived personal tokens.

## GitHub Actions Pinning

Workflow actions are pinned to full commit SHAs with comments that identify the reviewed action version. The current pins were resolved from upstream action tags with `git ls-remote`.

Maintainers updating actions should:

- Let Dependabot open GitHub Actions update pull requests when possible.
- Resolve the new action tag to a full 40-character commit SHA from the upstream repository.
- Review the upstream release notes before updating the pin.
- Keep the version comment beside the pinned SHA.
- Require normal CI, docs, Docker, and CODEOWNERS review before merging workflow pin updates.
