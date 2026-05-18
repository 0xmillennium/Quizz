from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
sys.path.insert(0, str(Path(__file__).resolve().parents[2]))

from lib.env import EnvConfigError, load_env_file, require_keys
from quizz_http.config import DemoConfigError, load_demo_tool_config


class EnvConfigTests(unittest.TestCase):
    def test_loads_required_keys_from_env(self) -> None:
        with self._project("QUIZZ_HTTP_PORT=8081\nQUIZZ_DEFAULT_ADMIN_EMAIL=admin@example.com\n") as root:
            values = require_keys(load_env_file(root), ("QUIZZ_HTTP_PORT", "QUIZZ_DEFAULT_ADMIN_EMAIL"))

        self.assertEqual("8081", values["QUIZZ_HTTP_PORT"])
        self.assertEqual("admin@example.com", values["QUIZZ_DEFAULT_ADMIN_EMAIL"])

    def test_fails_when_env_missing(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            with self.assertRaisesRegex(DemoConfigError, r"\.env file not found"):
                load_demo_tool_config(Path(tmp))

    def test_fails_when_required_key_missing(self) -> None:
        with self._project("QUIZZ_HTTP_PORT=8081\n") as root:
            with self.assertRaisesRegex(DemoConfigError, "Missing required .env key: QUIZZ_DEFAULT_ADMIN_EMAIL"):
                load_demo_tool_config(root)

    def test_fails_when_required_key_blank(self) -> None:
        with self._project("QUIZZ_HTTP_PORT=8081\nQUIZZ_DEFAULT_ADMIN_EMAIL=\n") as root:
            with self.assertRaisesRegex(DemoConfigError, "Required .env key is blank: QUIZZ_DEFAULT_ADMIN_EMAIL"):
                load_demo_tool_config(root)

    def test_derives_base_url_from_quizz_http_port(self) -> None:
        with self._project("QUIZZ_HTTP_PORT=9090\nQUIZZ_DEFAULT_ADMIN_EMAIL=admin@example.com\n") as root:
            config = load_demo_tool_config(root)

        self.assertEqual(9090, config.http_port)
        self.assertEqual("http://localhost:9090", config.base_url)

    def test_rejects_quizz_base_url(self) -> None:
        with self._project(
            "QUIZZ_HTTP_PORT=8081\n"
            "QUIZZ_DEFAULT_ADMIN_EMAIL=admin@example.com\n"
            "QUIZZ_BASE_URL=http://localhost:8080\n"
        ) as root:
            with self.assertRaisesRegex(DemoConfigError, "QUIZZ_BASE_URL is not supported"):
                load_demo_tool_config(root)

    def test_rejects_postgres_password(self) -> None:
        self._assert_rejects_forbidden_secret("POSTGRES_PASSWORD")

    def test_rejects_spring_datasource_password(self) -> None:
        self._assert_rejects_forbidden_secret("SPRING_DATASOURCE_PASSWORD")

    def test_rejects_admin_password(self) -> None:
        self._assert_rejects_forbidden_secret("ADMIN_PASSWORD")

    def test_rejects_non_integer_quizz_http_port(self) -> None:
        with self._project("QUIZZ_HTTP_PORT=abc\nQUIZZ_DEFAULT_ADMIN_EMAIL=admin@example.com\n") as root:
            with self.assertRaisesRegex(DemoConfigError, "QUIZZ_HTTP_PORT must be an integer"):
                load_demo_tool_config(root)

    def test_rejects_invalid_port_less_than_one(self) -> None:
        with self._project("QUIZZ_HTTP_PORT=0\nQUIZZ_DEFAULT_ADMIN_EMAIL=admin@example.com\n") as root:
            with self.assertRaisesRegex(DemoConfigError, "QUIZZ_HTTP_PORT must be between 1 and 65535"):
                load_demo_tool_config(root)

    def test_rejects_invalid_port_greater_than_65535(self) -> None:
        with self._project("QUIZZ_HTTP_PORT=65536\nQUIZZ_DEFAULT_ADMIN_EMAIL=admin@example.com\n") as root:
            with self.assertRaisesRegex(DemoConfigError, "QUIZZ_HTTP_PORT must be between 1 and 65535"):
                load_demo_tool_config(root)

    def test_accepts_quoted_values(self) -> None:
        with self._project('QUIZZ_HTTP_PORT="8081"\nQUIZZ_DEFAULT_ADMIN_EMAIL=\'admin@example.com\'\n') as root:
            config = load_demo_tool_config(root)

        self.assertEqual(8081, config.http_port)
        self.assertEqual("admin@example.com", config.default_admin_email)

    def test_ignores_comments_and_blank_lines(self) -> None:
        with self._project(
            "# comment\n"
            "\n"
            "QUIZZ_HTTP_PORT=8081\n"
            "\n"
            "# another comment\n"
            "QUIZZ_DEFAULT_ADMIN_EMAIL=admin@example.com\n"
        ) as root:
            config = load_demo_tool_config(root)

        self.assertEqual("http://localhost:8081", config.base_url)

    def _assert_rejects_forbidden_secret(self, key: str) -> None:
        with self._project(
            "QUIZZ_HTTP_PORT=8081\n"
            "QUIZZ_DEFAULT_ADMIN_EMAIL=admin@example.com\n"
            f"{key}=secret\n"
        ) as root:
            with self.assertRaisesRegex(DemoConfigError, f"{key} must not be stored in .env"):
                load_demo_tool_config(root)

    def _project(self, env_contents: str):
        return TempProject(env_contents)


class TempProject:
    def __init__(self, env_contents: str) -> None:
        self.env_contents = env_contents
        self.tmp: tempfile.TemporaryDirectory[str] | None = None
        self.root: Path | None = None

    def __enter__(self) -> Path:
        self.tmp = tempfile.TemporaryDirectory()
        self.root = Path(self.tmp.name)
        (self.root / ".env").write_text(self.env_contents, encoding="utf-8")
        return self.root

    def __exit__(self, *args: object) -> None:
        if self.tmp is not None:
            self.tmp.cleanup()


if __name__ == "__main__":
    unittest.main()
