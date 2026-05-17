from __future__ import annotations

from dataclasses import dataclass
from html.parser import HTMLParser

from .errors import CsrfTokenNotFoundError


@dataclass(frozen=True)
class CsrfToken:
    parameter_name: str
    value: str


class _CsrfParser(HTMLParser):
    def __init__(self) -> None:
        super().__init__(convert_charrefs=True)
        self.token: CsrfToken | None = None

    def handle_starttag(self, tag: str, attrs: list[tuple[str, str | None]]) -> None:
        if tag.lower() != "input" or self.token is not None:
            return

        values = {name.lower(): value or "" for name, value in attrs}
        input_type = values.get("type", "").lower()
        name = values.get("name", "")
        value = values.get("value", "")
        if input_type == "hidden" and name and value and "csrf" in name.lower():
            self.token = CsrfToken(name, value)


def extract_csrf_token(html: str) -> CsrfToken:
    parser = _CsrfParser()
    parser.feed(html)
    if parser.token is None:
        raise CsrfTokenNotFoundError("CSRF token hidden input was not found.")
    return parser.token
