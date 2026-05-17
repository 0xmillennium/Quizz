(() => {
    const MIN_OPTIONS = 2;
    const MAX_OPTIONS = 6;

    document.querySelectorAll("[data-question-form]").forEach((form) => {
        const container = form.querySelector("[data-options-container]");
        const addButton = form.querySelector("[data-add-option]");

        if (!container || !addButton) {
            return;
        }

        const updateButtons = () => {
            const rows = container.querySelectorAll("[data-option-row]");
            addButton.disabled = rows.length >= MAX_OPTIONS;
            rows.forEach((row) => {
                const removeButton = row.querySelector("[data-remove-option]");
                if (removeButton) {
                    removeButton.disabled = rows.length <= MIN_OPTIONS;
                }
            });
        };

        const reindexRows = () => {
            container.querySelectorAll("[data-option-row]").forEach((row, index) => {
                const textInput = row.querySelector("input[type='text']");
                const correctInput = row.querySelector("input[type='checkbox']");

                if (textInput) {
                    textInput.name = `options[${index}].text`;
                    textInput.id = `options${index}.text`;
                }
                if (correctInput) {
                    correctInput.name = `options[${index}].correct`;
                    correctInput.id = `options${index}.correct`;
                }
            });
            updateButtons();
        };

        addButton.addEventListener("click", () => {
            const rows = container.querySelectorAll("[data-option-row]");
            if (rows.length >= MAX_OPTIONS) {
                return;
            }

            const newRow = rows[rows.length - 1].cloneNode(true);
            newRow.querySelectorAll("input").forEach((input) => {
                if (input.type === "checkbox") {
                    input.checked = false;
                } else {
                    input.value = "";
                }
            });
            newRow.querySelectorAll("p").forEach((error) => error.remove());
            container.appendChild(newRow);
            reindexRows();
        });

        container.addEventListener("click", (event) => {
            const removeButton = event.target.closest("[data-remove-option]");
            if (!removeButton) {
                return;
            }

            const rows = container.querySelectorAll("[data-option-row]");
            if (rows.length <= MIN_OPTIONS) {
                return;
            }

            removeButton.closest("[data-option-row]").remove();
            reindexRows();
        });

        reindexRows();
    });
})();
