from __future__ import annotations

import sys
from dataclasses import dataclass
from pathlib import Path

from .errors import DemoFixtureError


SCRIPTS_DIR = Path(__file__).resolve().parents[2]
if str(SCRIPTS_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPTS_DIR))

from lib.env import EnvConfigError, find_project_root, load_env_file, require_keys


class DemoConfigError(DemoFixtureError):
    pass


@dataclass(frozen=True)
class DemoToolConfig:
    http_port: int
    base_url: str
    default_admin_email: str


def load_demo_tool_config(project_root: Path | None = None) -> DemoToolConfig:
    try:
        root = project_root if project_root is not None else find_project_root(Path(__file__))
        values = require_keys(
            load_env_file(root),
            (
                "QUIZZ_HTTP_PORT",
                "QUIZZ_DEFAULT_ADMIN_EMAIL",
            ),
        )
        http_port = _parse_http_port(values["QUIZZ_HTTP_PORT"])
        return DemoToolConfig(
            http_port=http_port,
            base_url=f"http://localhost:{http_port}",
            default_admin_email=values["QUIZZ_DEFAULT_ADMIN_EMAIL"],
        )
    except EnvConfigError as exc:
        raise DemoConfigError(str(exc)) from exc


def _parse_http_port(value: str) -> int:
    try:
        port = int(value)
    except ValueError as exc:
        raise DemoConfigError("ERROR: QUIZZ_HTTP_PORT must be an integer.") from exc

    if port < 1 or port > 65535:
        raise DemoConfigError("ERROR: QUIZZ_HTTP_PORT must be between 1 and 65535.")

    return port
