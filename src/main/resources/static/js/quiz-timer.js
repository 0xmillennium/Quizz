document.addEventListener("DOMContentLoaded", () => {
    const timer = document.querySelector("[data-expires-at]");
    const form = document.getElementById("quiz-submit-form");
    const message = document.getElementById("quiz-timer-message");

    if (!timer) {
        return;
    }

    const expiresAt = new Date(timer.dataset.expiresAt).getTime();
    let submitted = false;

    function render() {
        const remainingMs = expiresAt - Date.now();
        if (remainingMs <= 0) {
            timer.textContent = "00:00";
            if (message) {
                message.textContent = "Time is up. Submitting your quiz.";
            }
            if (form && !submitted) {
                submitted = true;
                form.requestSubmit();
            }
            return;
        }

        const totalSeconds = Math.ceil(remainingMs / 1000);
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;
        timer.textContent = `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
        window.setTimeout(render, 1000);
    }

    if (form) {
        form.addEventListener("submit", () => {
            submitted = true;
        });
    }

    render();
});
