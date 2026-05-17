from __future__ import annotations

from .admin_catalog import FixtureResult


def print_results(title: str, results: list[FixtureResult]) -> None:
    print(f"\n{title}")
    print("=" * len(title))
    for result in results:
        print(f"[{result.status}] {result.name}: {result.message}")

    created = _count(results, "created")
    skipped = _count(results, "skipped")
    published = _count(results, "published")
    warnings = _count(results, "warning")
    print(
        f"Summary: created={created}, skipped={skipped}, "
        f"published={published}, warnings={warnings}"
    )


def _count(results: list[FixtureResult], status: str) -> int:
    return sum(1 for result in results if result.status == status)
