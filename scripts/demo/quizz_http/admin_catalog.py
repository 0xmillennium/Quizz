from __future__ import annotations

from dataclasses import dataclass
from html.parser import HTMLParser
from urllib.parse import urlparse

from .client import QuizzHttpClient
from .csrf import extract_csrf_token
from .errors import FixtureValidationError, RemoteOperationError
from .fixtures import DemoCategory, DemoQuestion, DemoQuiz
from .forms import extract_entity_id_from_href, extract_select_options


@dataclass(frozen=True)
class FixtureResult:
    name: str
    status: str
    message: str


@dataclass(frozen=True)
class AdminQuizRef:
    id: int
    title: str
    status: str


@dataclass(frozen=True)
class _TableRow:
    cells: list[str]
    hrefs: list[str]


@dataclass(frozen=True)
class _QuestionChoice:
    id: str
    text: str


class AdminCatalogClient:
    def __init__(self, http: QuizzHttpClient) -> None:
        self.http = http

    def ensure_categories(self, categories: list[DemoCategory]) -> list[FixtureResult]:
        results: list[FixtureResult] = []
        existing = self._category_names()
        for category in categories:
            if category.name in existing:
                results.append(FixtureResult(category.name, "skipped", "Category already exists."))
                continue

            form = self.http.get("/admin/categories/new")
            csrf = extract_csrf_token(form.body)
            self.http.post_form(
                "/admin/categories",
                {
                    "name": category.name,
                    "description": category.description,
                    csrf.parameter_name: csrf.value,
                },
            )
            existing = self._category_names()
            if category.name not in existing:
                raise RemoteOperationError(f"Category was not created: {category.name}")
            results.append(FixtureResult(category.name, "created", "Category created."))
        return results

    def ensure_questions(self, questions: list[DemoQuestion]) -> list[FixtureResult]:
        results: list[FixtureResult] = []
        existing = self._question_texts()
        for question in questions:
            if question.text in existing:
                results.append(FixtureResult(question.text, "skipped", "Question already exists."))
                continue

            form = self.http.get("/admin/questions/new")
            csrf = extract_csrf_token(form.body)
            categories = extract_select_options(form.body, "categoryId")
            category_id = categories.get(question.category)
            if category_id is None:
                raise FixtureValidationError(
                    f"Missing active category '{question.category}' for question '{question.text}'."
                )

            data: dict[str, str | list[str]] = {
                "categoryId": category_id,
                "text": question.text,
                csrf.parameter_name: csrf.value,
            }
            for index, option in enumerate(question.options):
                data[f"options[{index}].text"] = option.text
                if option.correct:
                    data[f"options[{index}].correct"] = "on"

            self.http.post_form("/admin/questions", data)
            existing = self._question_texts()
            if question.text not in existing:
                raise RemoteOperationError(f"Question was not created: {question.text}")
            results.append(FixtureResult(question.text, "created", "Question created."))
        return results

    def ensure_quizzes(self, quizzes: list[DemoQuiz]) -> list[FixtureResult]:
        results: list[FixtureResult] = []
        existing = self._quiz_refs_by_title()
        for quiz in quizzes:
            existing_quiz = existing.get(quiz.title)
            if existing_quiz is not None:
                status = existing_quiz.status.upper()
                if status == "PUBLISHED":
                    results.append(FixtureResult(quiz.title, "skipped", "Quiz already published."))
                elif status == "DRAFT" and quiz.publish:
                    self._publish(existing_quiz.id, quiz.title)
                    results.append(FixtureResult(quiz.title, "published", "Existing draft published."))
                    existing = self._quiz_refs_by_title()
                elif status == "ARCHIVED":
                    results.append(FixtureResult(quiz.title, "warning", "Quiz is archived; skipped."))
                else:
                    results.append(FixtureResult(quiz.title, "skipped", f"Quiz already exists as {status}."))
                continue

            form = self.http.get("/admin/quizzes/new")
            csrf = extract_csrf_token(form.body)
            categories = extract_select_options(form.body, "categoryId")
            category_id = categories.get(quiz.category)
            if category_id is None:
                raise FixtureValidationError(
                    f"Missing active category '{quiz.category}' for quiz '{quiz.title}'."
                )

            question_choices = self._question_choices_from_create_form(form.body)
            question_ids: list[str] = []
            for question_text in quiz.questions:
                question_id = question_choices.get(question_text)
                if question_id is None:
                    raise FixtureValidationError(
                        f"Missing active question '{question_text}' for quiz '{quiz.title}'."
                    )
                question_ids.append(question_id)

            self.http.post_form(
                "/admin/quizzes",
                {
                    "title": quiz.title,
                    "description": quiz.description,
                    "categoryId": category_id,
                    "durationMinutes": str(quiz.duration_minutes),
                    "questionCount": str(quiz.question_count),
                    "attemptLimit": str(quiz.attempt_limit),
                    "retakeCooldownMinutes": str(quiz.retake_cooldown_minutes),
                    "questionIds": question_ids,
                    csrf.parameter_name: csrf.value,
                },
            )
            existing = self._quiz_refs_by_title()
            created = existing.get(quiz.title)
            if created is None:
                raise RemoteOperationError(f"Quiz was not created: {quiz.title}")
            if quiz.publish:
                self._publish(created.id, quiz.title)
                existing = self._quiz_refs_by_title()
                results.append(FixtureResult(quiz.title, "published", "Quiz created and published."))
            else:
                results.append(FixtureResult(quiz.title, "created", "Quiz draft created."))
        return results

    def _category_names(self) -> set[str]:
        response = self.http.get("/admin/categories")
        return {row.cells[0] for row in _extract_table_rows(response.body) if row.cells}

    def _question_texts(self) -> set[str]:
        response = self.http.get("/admin/questions")
        return {row.cells[0] for row in _extract_table_rows(response.body) if row.cells}

    def _quiz_refs_by_title(self) -> dict[str, AdminQuizRef]:
        response = self.http.get("/admin/quizzes")
        refs: dict[str, AdminQuizRef] = {}
        for row in _extract_table_rows(response.body):
            if len(row.cells) < 4:
                continue
            quiz_id = None
            for href in row.hrefs:
                quiz_id = extract_entity_id_from_href(_path_only(href), "/admin/quizzes/")
                if quiz_id is not None:
                    break
            if quiz_id is not None:
                refs[row.cells[0]] = AdminQuizRef(id=quiz_id, title=row.cells[0], status=row.cells[3])
        return refs

    def _question_choices_from_create_form(self, html: str) -> dict[str, str]:
        parser = _QuestionChoiceParser()
        parser.feed(html)
        return {choice.text: choice.id for choice in parser.choices if choice.id and choice.text}

    def _publish(self, quiz_id: int, title: str) -> None:
        detail = self.http.get(f"/admin/quizzes/{quiz_id}")
        csrf = extract_csrf_token(detail.body)
        response = self.http.post_form(
            f"/admin/quizzes/{quiz_id}/publish",
            {csrf.parameter_name: csrf.value},
        )
        rows = self._quiz_refs_by_title()
        published = rows.get(title)
        if published is None or published.status.upper() != "PUBLISHED":
            if "PUBLISHED" not in response.body:
                raise RemoteOperationError(f"Quiz was not published: {title}")


class _TableParser(HTMLParser):
    def __init__(self) -> None:
        super().__init__(convert_charrefs=True)
        self.rows: list[_TableRow] = []
        self.in_row = False
        self.in_cell = False
        self.current_cells: list[str] = []
        self.current_hrefs: list[str] = []
        self.current_cell_text: list[str] = []

    def handle_starttag(self, tag: str, attrs: list[tuple[str, str | None]]) -> None:
        lowered = tag.lower()
        values = {name: value or "" for name, value in attrs}
        if lowered == "tr":
            self.in_row = True
            self.current_cells = []
            self.current_hrefs = []
        elif self.in_row and lowered == "td":
            self.in_cell = True
            self.current_cell_text = []
        elif self.in_row and lowered == "a" and values.get("href"):
            self.current_hrefs.append(values["href"])

    def handle_data(self, data: str) -> None:
        if self.in_cell:
            self.current_cell_text.append(data)

    def handle_endtag(self, tag: str) -> None:
        lowered = tag.lower()
        if lowered == "td" and self.in_cell:
            text = " ".join("".join(self.current_cell_text).split())
            self.current_cells.append(text)
            self.in_cell = False
            self.current_cell_text = []
        elif lowered == "tr" and self.in_row:
            if self.current_cells:
                self.rows.append(_TableRow(cells=self.current_cells, hrefs=self.current_hrefs))
            self.in_row = False


class _QuestionChoiceParser(HTMLParser):
    def __init__(self) -> None:
        super().__init__(convert_charrefs=True)
        self.choices: list[_QuestionChoice] = []
        self.in_question_row = False
        self.in_span = False
        self.current_id = ""
        self.current_spans: list[str] = []
        self.current_span_text: list[str] = []

    def handle_starttag(self, tag: str, attrs: list[tuple[str, str | None]]) -> None:
        lowered = tag.lower()
        values = {name: value or "" for name, value in attrs}
        if lowered == "div" and "data-question-row" in values:
            self.in_question_row = True
            self.current_id = ""
            self.current_spans = []
        elif self.in_question_row and lowered == "input" and values.get("name") == "questionIds":
            self.current_id = values.get("value", "")
        elif self.in_question_row and lowered == "span":
            self.in_span = True
            self.current_span_text = []

    def handle_data(self, data: str) -> None:
        if self.in_span:
            self.current_span_text.append(data)

    def handle_endtag(self, tag: str) -> None:
        lowered = tag.lower()
        if lowered == "span" and self.in_span:
            self.current_spans.append(" ".join("".join(self.current_span_text).split()))
            self.in_span = False
        elif lowered == "div" and self.in_question_row:
            text = self.current_spans[0] if self.current_spans else ""
            self.choices.append(_QuestionChoice(id=self.current_id, text=text))
            self.in_question_row = False


def _extract_table_rows(html: str) -> list[_TableRow]:
    parser = _TableParser()
    parser.feed(html)
    return parser.rows


def _path_only(href: str) -> str:
    parsed = urlparse(href)
    return parsed.path if parsed.scheme or parsed.netloc else href
