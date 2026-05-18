document.addEventListener("DOMContentLoaded", () => {
    const token = document.querySelector("meta[name='_csrf']")?.content;
    const header = document.querySelector("meta[name='_csrf_header']")?.content;
    const form = document.getElementById("quiz-submit-form");
    const globalStatus = document.querySelector("[data-global-autosave-status]");
    const progressPanel = document.querySelector("[data-progress-total]");
    const answeredCount = document.querySelector("[data-progress-answered]");
    const unansweredCount = document.querySelector("[data-progress-unanswered]");
    const totalCount = document.querySelector("[data-progress-total-text]");
    const revisions = new Map();

    function statusElement(questionId) {
        return document.querySelector(`[data-autosave-status-for="${questionId}"]`);
    }

    function setStatus(questionId, text) {
        const element = statusElement(questionId);
        if (element) {
            element.textContent = text;
        }
        if (globalStatus) {
            globalStatus.textContent = text;
        }
    }

    function questionCards() {
        return Array.from(document.querySelectorAll("[data-question-card]"));
    }

    function updateOptionCards() {
        document.querySelectorAll(".option-card").forEach((card) => {
            const input = card.querySelector("input[type='radio']");
            card.classList.toggle("option-card-selected", Boolean(input?.checked));
        });
    }

    function updateProgress() {
        const cards = questionCards();
        const total = progressPanel ? Number(progressPanel.dataset.progressTotal || cards.length) : cards.length;
        const answered = cards.filter((card) => card.querySelector("input[type='radio']:checked")).length;
        const unanswered = Math.max(total - answered, 0);

        if (answeredCount) {
            answeredCount.textContent = String(answered);
        }
        if (unansweredCount) {
            unansweredCount.textContent = String(unanswered);
        }
        if (totalCount) {
            totalCount.textContent = String(total);
        }
    }

    function unansweredTotal() {
        const cards = questionCards();
        return cards.filter((card) => !card.querySelector("input[type='radio']:checked")).length;
    }

    document.querySelectorAll("input[type='radio'][data-autosave-url]").forEach((radio) => {
        const questionId = radio.dataset.attemptQuestionId;
        revisions.set(questionId, Number(radio.dataset.answerRevision || "0"));

        radio.addEventListener("change", async () => {
            const nextRevision = (revisions.get(questionId) || 0) + 1;
            revisions.set(questionId, nextRevision);
            setStatus(questionId, "Saving...");
            updateOptionCards();
            updateProgress();

            const body = new URLSearchParams();
            body.set("selectedOptionId", radio.value);
            body.set("answerRevision", String(nextRevision));

            try {
                const response = await fetch(radio.dataset.autosaveUrl, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded",
                        ...(token && header ? {[header]: token} : {})
                    },
                    body
                });
                if (!response.ok) {
                    throw new Error("Autosave failed");
                }
                const data = await response.json();
                revisions.set(questionId, data.answerRevision);
                if (data.autoSubmitted && data.redirectUrl) {
                    window.location.assign(data.redirectUrl);
                    return;
                }
                if (data.stale) {
                    setStatus(questionId, "Stale save ignored.");
                    return;
                }
                setStatus(questionId, data.saved ? "Saved" : "Not saved. Check connection.");
            } catch (error) {
                setStatus(questionId, "Not saved. Check connection.");
            }
        });
    });

    if (form) {
        form.addEventListener("submit", (event) => {
            const remaining = unansweredTotal();
            if (remaining > 0 && !window.confirm("You have unanswered questions. Submit anyway?")) {
                event.preventDefault();
            }
        });
    }

    updateOptionCards();
    updateProgress();
});
