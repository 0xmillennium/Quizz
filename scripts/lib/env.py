from __future__ import annotations

import re
from pathlib import Path
from typing import Iterable, Mapping


class EnvConfigError(Exception):
    pass


FORBIDDEN_SECRET_KEYS = {
    "POSTGRES_PASSWORD",
    "SPRING_DATASOURCE_PASSWORD",
    "ADMIN_PASSWORD",
    "QUIZZ_ADMIN_PASSWORD",
}

KEY_VALUE_PATTERN = re.compile(r"^([A-Za-z_][A-Za-z0-9_]*)=(.*)$")


def find_project_root(start: Path) -> Path:
    current = start.resolve()
    if current.is_file():
        current = current.parent

    for candidate in (current, *current.parents):
        if (candidate / "pom.xml").is_file() and (candidate / "docker-compose.yml").is_file():
            return candidate
        if (candidate / ".env").is_file() or (candidate / ".env.example").is_file():
            return candidate

    raise EnvConfigError("Could not resolve project root.")


def load_env_file(project_root: Path) -> dict[str, str]:
    env_file = project_root / ".env"
    if not env_file.is_file():
        raise EnvConfigError(
            "ERROR: .env file not found.\n"
            "Create it with:\n"
            "  cp .env.example .env\n"
            "Then review values before running this script."
        )

    values: dict[str, str] = {}
    for line_number, raw_line in enumerate(env_file.read_text(encoding="utf-8").splitlines(), start=1):
        line = raw_line.removesuffix("\r")
        if not line.strip() or line.lstrip().startswith("#"):
            continue
        if line.lstrip().startswith("export "):
            raise EnvConfigError(f"Unsupported .env syntax on line {line_number}: export is not allowed.")

        match = KEY_VALUE_PATTERN.fullmatch(line)
        if not match:
            raise EnvConfigError(f"Unsupported .env syntax on line {line_number}. Use KEY=value.")

        key, value = match.groups()
        if key in values:
            raise EnvConfigError(f"Duplicate .env key: {key}")
        if "$(" in value or "`" in value:
            raise EnvConfigError(
                f"Unsupported .env value for {key}: command substitution is not allowed."
            )

        values[key] = _unquote(value)

    reject_forbidden_keys(values)
    return values


def require_keys(values: Mapping[str, str], keys: Iterable[str]) -> dict[str, str]:
    required: dict[str, str] = {}
    for key in keys:
        if key not in values:
            raise EnvConfigError(f"ERROR: Missing required .env key: {key}")
        value = values[key]
        if not value.strip():
            raise EnvConfigError(f"ERROR: Required .env key is blank: {key}")
        required[key] = value
    return required


def reject_forbidden_keys(values: Mapping[str, str]) -> None:
    if "QUIZZ_BASE_URL" in values:
        raise EnvConfigError("ERROR: QUIZZ_BASE_URL is not supported. Configure QUIZZ_HTTP_PORT instead.")

    for key in sorted(FORBIDDEN_SECRET_KEYS):
        if key in values:
            raise EnvConfigError(f"ERROR: {key} must not be stored in .env. Use Docker secrets.")


def _unquote(value: str) -> str:
    if len(value) >= 2 and value[0] == value[-1] and value[0] in {"'", '"'}:
        return value[1:-1]
    return value
