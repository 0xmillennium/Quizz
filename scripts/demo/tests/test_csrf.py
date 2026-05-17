from __future__ import annotations

import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from quizz_http.csrf import extract_csrf_token
from quizz_http.errors import CsrfTokenNotFoundError


class CsrfTests(unittest.TestCase):
    def test_extracts_csrf_token_from_hidden_input(self) -> None:
        token = extract_csrf_token('<input type="hidden" name="_csrf" value="abc123">')

        self.assertEqual("_csrf", token.parameter_name)
        self.assertEqual("abc123", token.value)

    def test_supports_attributes_in_different_order(self) -> None:
        token = extract_csrf_token('<input value="xyz" name="_csrf" type="hidden">')

        self.assertEqual("_csrf", token.parameter_name)
        self.assertEqual("xyz", token.value)

    def test_raises_when_token_missing(self) -> None:
        with self.assertRaises(CsrfTokenNotFoundError):
            extract_csrf_token("<form><input name='email'></form>")


if __name__ == "__main__":
    unittest.main()
