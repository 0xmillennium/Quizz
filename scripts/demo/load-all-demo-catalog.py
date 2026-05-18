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
from quizz_http.fixtures import load_categories, load_questions, load_quizzes
from quizz_http.output import print_results


SCRIPT_DIR = Path(__file__).resolve().parent


def main() -> int:
    parser = argparse.ArgumentParser(description="Load all demo catalog fixtures through admin HTTP endpoints.")
    parser.parse_args()

    try:
        config = load_demo_tool_config()
        categories = load_categories(SCRIPT_DIR / "data" / "categories.json")
        questions = load_questions(SCRIPT_DIR / "data" / "questions.json")
        quizzes = load_quizzes(SCRIPT_DIR / "data" / "quizzes.json")

        http = _login(config)
        admin_catalog = AdminCatalogClient(http)

        category_results = admin_catalog.ensure_categories(categories)
        print_results("Demo Categories", category_results)

        question_results = admin_catalog.ensure_questions(questions)
        print_results("Demo Questions", question_results)

        quiz_results = admin_catalog.ensure_quizzes(quizzes)
        print_results("Demo Quizzes", quiz_results)

        print_results("Demo Catalog Total", category_results + question_results + quiz_results)
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
