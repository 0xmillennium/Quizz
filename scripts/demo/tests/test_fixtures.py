from __future__ import annotations

import json
import sys
import tempfile
import unittest
from pathlib import Path
from typing import Any

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from quizz_http.errors import FixtureValidationError
from quizz_http.fixtures import load_categories, load_questions, load_quizzes


class FixtureLoaderTests(unittest.TestCase):
    def test_loads_valid_category_fixture(self) -> None:
        path = self._fixture([{"name": "Demo Java", "description": "Basics"}])

        categories = load_categories(path)

        self.assertEqual("Demo Java", categories[0].name)
        self.assertEqual("Basics", categories[0].description)

    def test_rejects_blank_category_name(self) -> None:
        path = self._fixture([{"name": " ", "description": "Basics"}])

        with self.assertRaises(FixtureValidationError):
            load_categories(path)

    def test_loads_valid_question_fixture(self) -> None:
        path = self._fixture([
            {
                "category": "Demo Java",
                "text": "Question?",
                "options": [
                    {"text": "A", "correct": True},
                    {"text": "B", "correct": False},
                ],
            }
        ])

        questions = load_questions(path)

        self.assertEqual("Question?", questions[0].text)
        self.assertEqual(2, len(questions[0].options))

    def test_rejects_question_with_zero_correct_answers(self) -> None:
        path = self._question_fixture(False, False)

        with self.assertRaises(FixtureValidationError):
            load_questions(path)

    def test_rejects_question_with_multiple_correct_answers(self) -> None:
        path = self._question_fixture(True, True)

        with self.assertRaises(FixtureValidationError):
            load_questions(path)

    def test_rejects_question_with_fewer_than_2_options(self) -> None:
        path = self._fixture([
            {
                "category": "Demo Java",
                "text": "Question?",
                "options": [{"text": "A", "correct": True}],
            }
        ])

        with self.assertRaises(FixtureValidationError):
            load_questions(path)

    def test_loads_valid_quiz_fixture(self) -> None:
        path = self._fixture([
            {
                "title": "Demo Quiz",
                "description": "Description",
                "category": "Demo Java",
                "durationMinutes": 10,
                "questions": ["Question?"],
                "publish": True,
            }
        ])

        quizzes = load_quizzes(path)

        self.assertEqual("Demo Quiz", quizzes[0].title)
        self.assertEqual(10, quizzes[0].duration_minutes)

    def test_rejects_quiz_with_invalid_duration(self) -> None:
        path = self._quiz_fixture(duration=181)

        with self.assertRaises(FixtureValidationError):
            load_quizzes(path)

    def test_rejects_quiz_with_duplicate_question_names(self) -> None:
        path = self._quiz_fixture(questions=["Question?", "Question?"])

        with self.assertRaises(FixtureValidationError):
            load_quizzes(path)

    def test_rejects_quiz_without_questions(self) -> None:
        path = self._quiz_fixture(questions=[])

        with self.assertRaises(FixtureValidationError):
            load_quizzes(path)

    def _question_fixture(self, first_correct: bool, second_correct: bool) -> Path:
        return self._fixture([
            {
                "category": "Demo Java",
                "text": "Question?",
                "options": [
                    {"text": "A", "correct": first_correct},
                    {"text": "B", "correct": second_correct},
                ],
            }
        ])

    def _quiz_fixture(
        self,
        duration: int = 10,
        questions: list[str] | None = None,
    ) -> Path:
        return self._fixture([
            {
                "title": "Demo Quiz",
                "description": "Description",
                "category": "Demo Java",
                "durationMinutes": duration,
                "questions": ["Question?"] if questions is None else questions,
                "publish": True,
            }
        ])

    def _fixture(self, data: list[dict[str, Any]]) -> Path:
        handle = tempfile.NamedTemporaryFile("w", encoding="utf-8", delete=False)
        with handle:
            json.dump(data, handle)
        path = Path(handle.name)
        self.addCleanup(path.unlink, missing_ok=True)
        return path

if __name__ == "__main__":
    unittest.main()
