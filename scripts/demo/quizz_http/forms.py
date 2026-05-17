from __future__ import annotations

from html import unescape
from html.parser import HTMLParser

from .errors import FixtureValidationError


class _SelectParser(HTMLParser):
    def __init__(self, select_name: str) -> None:
        super().__init__(convert_charrefs=True)
        self.select_name = select_name
        self.in_select = False
        self.in_option = False
        self.found_select = False
        self.current_value = ""
        self.current_text: list[str] = []
        self.options: dict[str, str] = {}

    def handle_starttag(self, tag: str, attrs: list[tuple[str, str | None]]) -> None:
        values = {name: value or "" for name, value in attrs}
        if tag.lower() == "select" and values.get("name") == self.select_name:
            self.in_select = True
            self.found_select = True
        elif self.in_select and tag.lower() == "option":
            self.in_option = True
            self.current_value = values.get("value", "")
            self.current_text = []

    def handle_data(self, data: str) -> None:
        if self.in_option:
            self.current_text.append(data)

    def handle_endtag(self, tag: str) -> None:
        lowered = tag.lower()
        if lowered == "option" and self.in_option:
            text = "".join(self.current_text).strip()
            if self.current_value:
                self.options[text] = self.current_value
            self.in_option = False
            self.current_value = ""
            self.current_text = []
        elif lowered == "select" and self.in_select:
            self.in_select = False


def extract_select_options(html: str, select_name: str) -> dict[str, str]:
    """Return visible option text to value for a named select.

    Raises FixtureValidationError when the select is missing, because these
    scripts need the field to submit a valid admin form.
    """

    parser = _SelectParser(select_name)
    parser.feed(html)
    if not parser.found_select:
        raise FixtureValidationError(f"Select field '{select_name}' was not found.")
    return parser.options


def extract_entity_id_from_href(href: str, prefix: str) -> int | None:
    if not href.startswith(prefix):
        return None
    rest = href[len(prefix):]
    first_segment = rest.split("/", 1)[0]
    if not first_segment.isdigit():
        return None
    return int(first_segment)


def contains_text(html: str, text: str) -> bool:
    return text in unescape(html)
