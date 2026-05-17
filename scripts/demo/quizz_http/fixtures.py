from __future__ import annotations

from dataclasses import dataclass
import json
from pathlib import Path
from typing import Any

from .errors import FixtureValidationError


@dataclass(frozen=True)
class DemoCategory:
    name: str
    description: str


@dataclass(frozen=True)
class DemoAnswerOption:
    text: str
    correct: bool


@dataclass(frozen=True)
class DemoQuestion:
    category: str
    text: str
    options: list[DemoAnswerOption]


@dataclass(frozen=True)
class DemoQuiz:
    title: str
    description: str
    category: str
    duration_minutes: int
    questions: list[str]
    publish: bool


def load_categories(path: Path) -> list[DemoCategory]:
    raw = _load_json_list(path)
    categories: list[DemoCategory] = []
    for index, item in enumerate(raw):
        if not isinstance(item, dict):
            raise FixtureValidationError(f"Category #{index + 1} must be an object.")
        name = _required_str(item, "name", f"Category #{index + 1}")
        description = _optional_str(item, "description", f"Category #{index + 1}")
        categories.append(DemoCategory(name=name, description=description))
    return categories


def load_questions(path: Path) -> list[DemoQuestion]:
    raw = _load_json_list(path)
    questions: list[DemoQuestion] = []
    for index, item in enumerate(raw):
        context = f"Question #{index + 1}"
        if not isinstance(item, dict):
            raise FixtureValidationError(f"{context} must be an object.")
        category = _required_str(item, "category", context)
        text = _required_str(item, "text", context)
        options_raw = item.get("options")
        if not isinstance(options_raw, list):
            raise FixtureValidationError(f"{context} options must be a list.")
        if len(options_raw) < 2:
            raise FixtureValidationError(f"{context} must have at least 2 options.")

        options: list[DemoAnswerOption] = []
        for option_index, option_item in enumerate(options_raw):
            option_context = f"{context} option #{option_index + 1}"
            if not isinstance(option_item, dict):
                raise FixtureValidationError(f"{option_context} must be an object.")
            option_text = _required_str(option_item, "text", option_context)
            correct = option_item.get("correct")
            if not isinstance(correct, bool):
                raise FixtureValidationError(f"{option_context} correct must be boolean.")
            options.append(DemoAnswerOption(text=option_text, correct=correct))

        correct_count = sum(1 for option in options if option.correct)
        if correct_count != 1:
            raise FixtureValidationError(
                f"{context} must have exactly 1 correct option; found {correct_count}."
            )
        questions.append(DemoQuestion(category=category, text=text, options=options))
    return questions


def load_quizzes(path: Path) -> list[DemoQuiz]:
    raw = _load_json_list(path)
    quizzes: list[DemoQuiz] = []
    for index, item in enumerate(raw):
        context = f"Quiz #{index + 1}"
        if not isinstance(item, dict):
            raise FixtureValidationError(f"{context} must be an object.")
        title = _required_str(item, "title", context)
        description = _optional_str(item, "description", context)
        category = _required_str(item, "category", context)
        duration = item.get("durationMinutes")
        if not isinstance(duration, int) or not 1 <= duration <= 180:
            raise FixtureValidationError(f"{context} durationMinutes must be between 1 and 180.")
        questions_raw = item.get("questions")
        if not isinstance(questions_raw, list):
            raise FixtureValidationError(f"{context} questions must be a list.")
        if not questions_raw:
            raise FixtureValidationError(f"{context} must include at least 1 question.")
        questions = []
        for question_index, question in enumerate(questions_raw):
            if not isinstance(question, str) or not question.strip():
                raise FixtureValidationError(
                    f"{context} question #{question_index + 1} must be a non-blank string."
                )
            questions.append(question.strip())
        if len(set(questions)) != len(questions):
            raise FixtureValidationError(f"{context} questions must not contain duplicates.")
        publish = item.get("publish")
        if not isinstance(publish, bool):
            raise FixtureValidationError(f"{context} publish must be boolean.")
        quizzes.append(
            DemoQuiz(
                title=title,
                description=description,
                category=category,
                duration_minutes=duration,
                questions=questions,
                publish=publish,
            )
        )
    return quizzes


def _load_json_list(path: Path) -> list[Any]:
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except OSError as exc:
        raise FixtureValidationError(f"Unable to read fixture file {path}: {exc}") from exc
    except json.JSONDecodeError as exc:
        raise FixtureValidationError(f"Invalid JSON in {path}: {exc}") from exc
    if not isinstance(data, list):
        raise FixtureValidationError(f"Fixture file {path} must contain a JSON array.")
    return data


def _required_str(item: dict[str, Any], field: str, context: str) -> str:
    value = item.get(field)
    if not isinstance(value, str) or not value.strip():
        raise FixtureValidationError(f"{context} field '{field}' must be a non-blank string.")
    return value.strip()


def _optional_str(item: dict[str, Any], field: str, context: str) -> str:
    value = item.get(field, "")
    if not isinstance(value, str):
        raise FixtureValidationError(f"{context} field '{field}' must be a string.")
    return value
