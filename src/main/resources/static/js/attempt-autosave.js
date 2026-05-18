document.addEventListener("DOMContentLoaded", () => {
    const token = document.querySelector("meta[name='_csrf']")?.content;
    const header = document.querySelector("meta[name='_csrf_header']")?.content;
    const revisions = new Map();

    function statusElement(questionId) {
        return document.querySelector(`[data-autosave-status-for="${questionId}"]`);
    }

    function setStatus(questionId, text) {
        const element = statusElement(questionId);
        if (element) {
            element.textContent = text;
        }
    }

    document.querySelectorAll("input[type='radio'][data-autosave-url]").forEach((radio) => {
        const questionId = radio.dataset.attemptQuestionId;
        revisions.set(questionId, Number(radio.dataset.answerRevision || "0"));

        radio.addEventListener("change", async () => {
            const nextRevision = (revisions.get(questionId) || 0) + 1;
            revisions.set(questionId, nextRevision);
            setStatus(questionId, "Saving...");

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
});
