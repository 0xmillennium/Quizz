class DemoFixtureError(Exception):
    pass


class AuthenticationError(DemoFixtureError):
    pass


class CsrfTokenNotFoundError(DemoFixtureError):
    pass


class FixtureValidationError(DemoFixtureError):
    pass


class RemoteOperationError(DemoFixtureError):
    pass
