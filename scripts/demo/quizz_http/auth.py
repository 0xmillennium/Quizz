from __future__ import annotations

from .client import QuizzHttpClient
from .csrf import extract_csrf_token
from .errors import AuthenticationError, DemoFixtureError
from .forms import contains_text


class AuthClient:
    def __init__(self, http: QuizzHttpClient) -> None:
        self.http = http

    def login(self, email: str, password: str) -> None:
        try:
            login_page = self.http.get("/login")
            csrf = extract_csrf_token(login_page.body)
            self.http.post_form(
                "/login",
                {
                    "email": email,
                    "password": password,
                    csrf.parameter_name: csrf.value,
                },
            )
            self.assert_admin_access()
        except AuthenticationError:
            raise
        except DemoFixtureError as exc:
            raise AuthenticationError(f"Admin login failed: {exc}") from exc

    def assert_admin_access(self) -> None:
        try:
            response = self.http.get("/admin")
        except DemoFixtureError as exc:
            raise AuthenticationError(
                "Admin area is not accessible. Verify the account has ADMIN role."
            ) from exc

        if response.status != 200 or (
            not contains_text(response.body, "Dashboard")
            and not contains_text(response.body, "Quizz Admin")
        ):
            raise AuthenticationError(
                "Admin login did not reach the admin dashboard. Verify email, password, and role."
            )
