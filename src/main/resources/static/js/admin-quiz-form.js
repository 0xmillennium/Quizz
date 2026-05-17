document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector("[data-quiz-form]");
    if (!form) {
        return;
    }

    const categorySelect = form.querySelector("[data-quiz-category]");
    const questionRows = Array.from(form.querySelectorAll("[data-question-row]"));

    function filterQuestions() {
        const selectedCategoryId = categorySelect.value;

        questionRows.forEach(function (row) {
            const matchesCategory = !selectedCategoryId || row.dataset.categoryId === selectedCategoryId;
            row.hidden = !matchesCategory;

            if (!matchesCategory) {
                const checkbox = row.querySelector('input[type="checkbox"]');
                if (checkbox) {
                    checkbox.checked = false;
                }
            }
        });
    }

    categorySelect.addEventListener("change", filterQuestions);
    filterQuestions();
});
