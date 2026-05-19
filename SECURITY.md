# Security Policy

## Supported Versions

Quizz is under active development. Unless a release support policy is published later, security fixes are expected to be merged into the protected `master` branch through the normal pull request process, unless the project owner coordinates a private security fix branch.

## Reporting a Vulnerability

Do not report vulnerabilities as public issues.

Security vulnerabilities must be reported through GitHub Private Vulnerability Reporting for this repository. Maintainers must keep GitHub Private Vulnerability Reporting enabled before public launch and while accepting public security reports.

If the private reporting button is not available, the repository is misconfigured. Do not disclose vulnerability details publicly, and do not open a public issue with sensitive details. Maintainers must fix the repository setting before accepting public security reports.

## Do Not Report Vulnerabilities as Public Issues

Public issues are appropriate for non-sensitive bugs and feature requests. Security issues should stay in GitHub Private Vulnerability Reporting until maintainers have had an opportunity to investigate and respond.

## What to Report

Please report suspected vulnerabilities such as:

- Authentication or session bypass.
- CSRF bypass.
- ADMIN route authorization bypass.
- Correct answer leakage on the play page.
- `originalAnswerOptionId` leakage on the play page.
- Password hash exposure.
- User email or privacy exposure.
- Docker secret leakage.
- Unsafe GitHub Actions permission escalation.

## Out of Scope

The following are usually out of scope unless they demonstrate a concrete security impact in Quizz:

- Vulnerabilities requiring a compromised maintainer workstation.
- Issues only affecting unsupported local modifications.
- Denial-of-service reports without a practical exploit path.
- Reports based only on missing security headers without a demonstrated risk.

## Handling Accidentally Exposed Secrets

If a secret is accidentally exposed, revoke or rotate it immediately. Remove the exposed value from active configuration and notify maintainers privately. Do not paste secret values into public issues, pull requests, logs, or screenshots.

## Expected Response Process

Maintainers should acknowledge the report when practical, assess severity and reproducibility, prepare a fix or mitigation, and coordinate disclosure timing with the reporter. Response timing depends on maintainer availability and issue complexity, so no fixed response window is promised.
