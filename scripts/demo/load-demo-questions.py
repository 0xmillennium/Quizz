#!/usr/bin/env python3
from __future__ import annotations

import argparse
import getpass
import sys
from pathlib import Path

from quizz_http.admin_catalog import AdminCatalogClient
from quizz_http.auth import AuthClient
from quizz_http.client import QuizzHttpClient
from quizz_http.config import DemoToolConfig, load_demo_tool_config
from quizz_http.errors import DemoFixtureError
from quizz_http.fixtures import load_questions
from quizz_http.output import print_results


SCRIPT_DIR = Path(__file__).resolve().parent


def main() -> int:
    parser = argparse.ArgumentParser(description="Load demo questions through admin HTTP endpoints.")
    parser.parse_args()

    try:
        config = load_demo_tool_config()
        questions = load_questions(SCRIPT_DIR / "data" / "questions.json")
        http = _login(config)
        results = AdminCatalogClient(http).ensure_questions(questions)
        print_results("Demo Questions", results)
        return 0
    except DemoFixtureError as exc:
        print(f"Error: {exc}", file=sys.stderr)
        return 1


def _login(config: DemoToolConfig) -> QuizzHttpClient:
    password = getpass.getpass(f"Admin password for {config.default_admin_email}: ")
    http = QuizzHttpClient(config.base_url)
    AuthClient(http).login(config.default_admin_email, password)
    return http


if __name__ == "__main__":
    sys.exit(main())
