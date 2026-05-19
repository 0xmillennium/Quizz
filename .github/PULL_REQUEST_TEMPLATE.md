## Summary

Describe the change and why it is needed.

Target `master` with this pull request. PR titles should follow Conventional Commits.

Maintainers use squash merge and should preserve the validated PR title as the squash commit title.

## Type of Change

- [ ] Feature
- [ ] Bug fix
- [ ] Documentation
- [ ] Test
- [ ] Refactor
- [ ] Build/CI/tooling
- [ ] Security

## Verification

Check the items that apply. Explain skipped checks.

- [ ] `mvn clean test`
- [ ] `mvn spotless:check`
- [ ] `mvn -q javadoc:javadoc`
- [ ] `python3 -m unittest discover scripts/demo/tests`
- [ ] `mkdocs build --strict`
- [ ] `docker compose config`
- [ ] `docker compose build`

## Safety Checklist

- [ ] No secrets or credentials added
- [ ] No play-page correctness leakage
- [ ] No `originalAnswerOptionId` exposed on play views
- [ ] Mutating actions remain POST + CSRF
- [ ] Docs updated for behavior changes
- [ ] ADR added/updated for architectural decisions

## Documentation Checklist

- [ ] Documentation updated where behavior changed
- [ ] Javadocs/package docs updated where public code documentation changed
- [ ] No unsupported `QUIZZ_BASE_URL` guidance added

## Notes for Reviewers

Call out areas that need closer review, known tradeoffs, skipped checks, or follow-up work.
