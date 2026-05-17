from __future__ import annotations

from dataclasses import dataclass
from http.cookiejar import CookieJar
from typing import Mapping
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode, urljoin
from urllib.request import HTTPCookieProcessor, Request, build_opener

from .errors import RemoteOperationError


USER_AGENT = "QuizzDemoFixtureClient/1.0"


@dataclass(frozen=True)
class HtmlResponse:
    url: str
    status: int
    body: str
    headers: Mapping[str, str]


class QuizzHttpClient:
    def __init__(self, base_url: str, timeout_seconds: int = 20) -> None:
        normalized = base_url.strip()
        if not normalized:
            raise RemoteOperationError("Base URL must not be blank.")
        self.base_url = normalized.rstrip("/") + "/"
        self.timeout_seconds = timeout_seconds
        self.cookie_jar = CookieJar()
        self.opener = build_opener(HTTPCookieProcessor(self.cookie_jar))

    def get(self, path: str) -> HtmlResponse:
        request = Request(
            self.absolute_url(path),
            headers={"User-Agent": USER_AGENT},
            method="GET",
        )
        return self._open(request)

    def post_form(self, path: str, data: Mapping[str, str | list[str]]) -> HtmlResponse:
        encoded = urlencode(data, doseq=True).encode("utf-8")
        request = Request(
            self.absolute_url(path),
            data=encoded,
            headers={
                "Content-Type": "application/x-www-form-urlencoded",
                "User-Agent": USER_AGENT,
            },
            method="POST",
        )
        return self._open(request)

    def absolute_url(self, path: str) -> str:
        return urljoin(self.base_url, path.lstrip("/"))

    def _open(self, request: Request) -> HtmlResponse:
        try:
            with self.opener.open(request, timeout=self.timeout_seconds) as response:
                status = response.getcode()
                body = response.read().decode("utf-8")
                headers = dict(response.headers.items())
                if status < 200 or status >= 300:
                    raise RemoteOperationError(
                        f"Unexpected HTTP status {status} for {response.geturl()}."
                    )
                return HtmlResponse(response.geturl(), status, body, headers)
        except HTTPError as exc:
            detail = ""
            try:
                detail = exc.read().decode("utf-8", errors="replace")
            except Exception:
                detail = ""
            suffix = f" Response body starts with: {detail[:200]}" if detail else ""
            raise RemoteOperationError(
                f"HTTP {exc.code} for {exc.geturl()}.{suffix}"
            ) from exc
        except URLError as exc:
            raise RemoteOperationError(f"Network error: {exc.reason}") from exc
