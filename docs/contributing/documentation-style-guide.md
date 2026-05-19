# Documentation Style Guide

This guide keeps Quizz documentation clear, accurate, and maintainable.

## Audience

Write for developers and maintainers who need to run, operate, understand, or change the project. Assume the reader is technical, but do not assume they already know this codebase.

## Tone

- Use clear English.
- Prefer direct statements over marketing language.
- State exact behavior and exact commands.
- Explain uncertainty only when the code or configuration does not answer a question.

## File Structure

Use the documentation information architecture:

- Getting Started: task-oriented onboarding.
- Architecture: explanations and design rationale.
- Operations: runtime behavior, configuration, secrets, and troubleshooting.
- Reference: exact facts and tables.
- Decisions: accepted architectural decisions.
- Contributing: documentation and code documentation standards.

## Headings

- Use one `#` heading per page.
- Use short, descriptive section headings.
- Keep related procedural steps under the same heading.

## Code Blocks

- Use fenced code blocks with a language when possible.
- Commands should be copy-pasteable.
- Do not put secrets in command examples.
- Prefer generated secret commands over literal secret values.

## Commands

When documenting a command:

1. State prerequisites.
2. Show the command.
3. State the expected result.
4. Link to troubleshooting when failure modes are common.

## Admonitions

Use admonitions for important warnings or notes. Keep them short and actionable.

```markdown
!!! warning
    Keep `.env` non-secret. Use Docker secrets for database passwords.
```

## Links

- Link to the primary source page instead of repeating long content.
- Use relative links inside the docs site.
- Keep README links short and high-level.

## Terminology

Use these terms consistently:

- attempt
- attempt right
- cooldown
- question pool
- snapshot
- autosave
- auto-submit
- completion reason
- command service
- query service
- read model
- Docker secret
- non-secret local configuration
- admin bootstrap

## Source of Truth

Use current code, migrations, scripts, Docker files, `.env.example`, tests, and Javadocs as source of truth. Do not document behavior that is not implemented.

Primary locations:

- Docker onboarding: `getting-started/docker.md`
- Docker internals: `operations/docker-runtime.md`
- Secrets concept: `operations/secrets.md`
- Exact config keys: `reference/configuration.md`
- Architecture explanations: `architecture/*.md`
- Scripts: `reference/scripts.md`
- Decisions: `decisions/*.md`

## What Not to Document

- Deleted legacy documents.
- Old project history.
- Unimplemented behavior.
- Fake secrets.
- Weak password examples.
- Long duplicated instructions already covered on the primary source page.

## Updating Docs

1. Change the primary source page for the topic.
2. Add short links from related pages when helpful.
3. Run `mkdocs build --strict`.
4. Run static searches from the verification checklist before submitting.
