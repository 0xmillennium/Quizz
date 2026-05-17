#!/usr/bin/env python3
from __future__ import annotations

import argparse
import getpass
import os
import sys
from pathlib import Path

from quizz_http.admin_catalog import AdminCatalogClient
from quizz_http.auth import AuthClient
from quizz_http.client import QuizzHttpClient
from quizz_http.errors import DemoFixtureError
from quizz_http.fixtures import load_categories
from quizz_http.output import print_results


DEFAULT_BASE_URL = "http://localhost:8080"
DEFAULT_ADMIN_EMAIL = "admin@example.com"
SCRIPT_DIR = Path(__file__).resolve().parent


def main() -> int:
    parser = argparse.ArgumentParser(description="Load demo categories through admin HTTP endpoints.")
    parser.add_argument("--base-url", default=None)
    parser.add_argument("--admin-email", default=DEFAULT_ADMIN_EMAIL)
    args = parser.parse_args()

    try:
        categories = load_categories(SCRIPT_DIR / "data" / "categories.json")
        http = _login(_base_url(args.base_url), args.admin_email)
        results = AdminCatalogClient(http).ensure_categories(categories)
        print_results("Demo Categories", results)
        return 0
    except DemoFixtureError as exc:
        print(f"Error: {exc}", file=sys.stderr)
        return 1


def _base_url(cli_value: str | None) -> str:
    return cli_value or os.environ.get("QUIZZ_BASE_URL") or DEFAULT_BASE_URL


def _login(base_url: str, email: str) -> QuizzHttpClient:
    password = getpass.getpass(f"Admin password for {email}: ")
    http = QuizzHttpClient(base_url)
    AuthClient(http).login(email, password)
    return http


if __name__ == "__main__":
    sys.exit(main())
