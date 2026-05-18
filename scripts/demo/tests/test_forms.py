from __future__ import annotations

import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from quizz_http.forms import contains_text, extract_entity_id_from_href, extract_select_options
from quizz_http.admin_catalog import _quiz_refs_from_admin_list


class FormParserTests(unittest.TestCase):
    def test_extracts_select_options(self) -> None:
        html = """
        <select name="categoryId">
            <option value="">Select category</option>
            <option value="10"> Demo Java </option>
            <option value="11">Demo Spring Boot</option>
        </select>
        """

        self.assertEqual(
            {"Demo Java": "10", "Demo Spring Boot": "11"},
            extract_select_options(html, "categoryId"),
        )

    def test_ignores_empty_option_values(self) -> None:
        html = '<select name="categoryId"><option value="">Choose</option><option value="5">A</option></select>'

        self.assertEqual({"A": "5"}, extract_select_options(html, "categoryId"))

    def test_extracts_entity_id_from_href(self) -> None:
        self.assertEqual(12, extract_entity_id_from_href("/admin/quizzes/12", "/admin/quizzes/"))
        self.assertEqual(12, extract_entity_id_from_href("/admin/quizzes/12/edit", "/admin/quizzes/"))
        self.assertEqual(5, extract_entity_id_from_href("/admin/categories/5/edit", "/admin/categories/"))
        self.assertIsNone(extract_entity_id_from_href("/admin/quizzes/new", "/admin/quizzes/"))

    def test_contains_text_works_with_html_body(self) -> None:
        self.assertTrue(contains_text("<body><h1>Demo &amp; Test</h1></body>", "Demo & Test"))
        self.assertFalse(contains_text("<body><h1>Demo</h1></body>", "demo"))

    def test_extracts_quiz_refs_from_admin_table_with_description(self) -> None:
        html = """
        <table>
            <tbody>
                <tr>
                    <td>
                        <strong>Demo Java Pool Quiz</strong>
                        <span class="table-meta">A randomized demo quiz.</span>
                    </td>
                    <td><span class="badge badge-primary">Demo Java</span></td>
                    <td><span class="badge badge-success">Published</span></td>
                    <td>12</td>
                    <td>6</td>
                    <td><a href="/admin/quizzes/42">View</a></td>
                </tr>
            </tbody>
        </table>
        """

        refs = _quiz_refs_from_admin_list(html)

        self.assertIn("Demo Java Pool Quiz", refs)
        self.assertEqual(42, refs["Demo Java Pool Quiz"].id)
        self.assertEqual("PUBLISHED", refs["Demo Java Pool Quiz"].status)


if __name__ == "__main__":
    unittest.main()
