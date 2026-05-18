document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector("[data-quiz-form]");
    if (!form) {
        return;
    }

    const categorySelect = form.querySelector("[data-quiz-category]");
    const questionCountInput = form.querySelector("[data-question-count]");
    const questionRows = Array.from(form.querySelectorAll("[data-question-row]"));
    const selectedCountElement = form.querySelector("[data-selected-count]");
    const requiredCountElement = form.querySelector("[data-required-count]");
    const poolHealthElement = form.querySelector("[data-pool-health]");
    const poolHealthMessage = form.querySelector("[data-pool-health-message]");

    function selectedQuestionCount() {
        return questionRows.filter(function (row) {
            const checkbox = row.querySelector('input[type="checkbox"]');
            return checkbox && checkbox.checked;
        }).length;
    }

    function requiredQuestionCount() {
        if (!questionCountInput) {
            return 0;
        }

        return Number.parseInt(questionCountInput.value, 10) || 0;
    }

    function updatePoolSummary() {
        const selectedCount = selectedQuestionCount();
        const requiredCount = requiredQuestionCount();
        const ready = selectedCount >= requiredCount && requiredCount > 0;

        if (selectedCountElement) {
            selectedCountElement.textContent = String(selectedCount);
        }
        if (requiredCountElement) {
            requiredCountElement.textContent = String(requiredCount || 0);
        }
        if (poolHealthElement) {
            poolHealthElement.classList.toggle("pool-health-ready", ready);
            poolHealthElement.classList.toggle("pool-health-warning", !ready);
            const label = poolHealthElement.querySelector("strong");
            if (label) {
                label.textContent = ready ? "Pool ready" : "Pool needs attention";
            }
        }
        if (poolHealthMessage) {
            poolHealthMessage.textContent = ready
                ? `${selectedCount} selected for ${requiredCount} questions per attempt.`
                : `Select at least ${requiredCount || 1} questions for this quiz.`;
        }
    }

    function filterQuestions() {
        const selectedCategoryId = categorySelect ? categorySelect.value : "";

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
        updatePoolSummary();
    }

    if (categorySelect) {
        categorySelect.addEventListener("change", filterQuestions);
    }
    if (questionCountInput) {
        questionCountInput.addEventListener("input", updatePoolSummary);
    }
    questionRows.forEach(function (row) {
        const checkbox = row.querySelector('input[type="checkbox"]');
        if (checkbox) {
            checkbox.addEventListener("change", updatePoolSummary);
        }
    });

    filterQuestions();
});
