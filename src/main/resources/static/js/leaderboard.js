document.addEventListener("DOMContentLoaded", () => {
    const quizSelect = document.getElementById("quizId");
    const categorySelect = document.getElementById("categoryId");

    if (!quizSelect || !categorySelect) {
        return;
    }

    const syncFilters = () => {
        const quizSelected = quizSelect.value !== "";
        const categorySelected = categorySelect.value !== "";

        if (quizSelected) {
            categorySelect.value = "";
            categorySelect.disabled = true;
            quizSelect.disabled = false;
            return;
        }

        if (categorySelected) {
            quizSelect.value = "";
            quizSelect.disabled = true;
            categorySelect.disabled = false;
            return;
        }

        quizSelect.disabled = false;
        categorySelect.disabled = false;
    };

    quizSelect.addEventListener("change", syncFilters);
    categorySelect.addEventListener("change", syncFilters);
    syncFilters();
});
