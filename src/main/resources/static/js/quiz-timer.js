document.addEventListener("DOMContentLoaded", () => {
    const timer = document.querySelector("[data-expires-at]");
    const form = document.getElementById("quiz-submit-form");
    const message = document.getElementById("quiz-timer-message");
    const token = document.querySelector("meta[name='_csrf']")?.content;
    const header = document.querySelector("meta[name='_csrf_header']")?.content;

    if (!timer) {
        return;
    }

    const expiresAt = new Date(timer.dataset.expiresAt).getTime();
    let submitted = false;

    async function autoSubmit() {
        if (!form || !form.dataset.autoSubmitUrl || submitted) {
            return;
        }
        submitted = true;
        try {
            const response = await fetch(form.dataset.autoSubmitUrl, {
                method: "POST",
                headers: token && header ? {[header]: token} : {}
            });
            if (!response.ok) {
                throw new Error("Auto-submit failed");
            }
            const data = await response.json();
            window.location.assign(data.redirectUrl);
        } catch (error) {
            if (message) {
                message.textContent = "Time is up. Reconnect and refresh to see your result.";
            }
        }
    }

    function render() {
        const remainingMs = expiresAt - Date.now();
        if (remainingMs <= 0) {
            timer.textContent = "00:00";
            timer.classList.remove("timer-warning");
            timer.classList.add("timer-danger");
            if (message) {
                message.textContent = "Time is up. Submitting your quiz.";
            }
            autoSubmit();
            return;
        }

        const totalSeconds = Math.ceil(remainingMs / 1000);
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;
        timer.textContent = `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
        timer.classList.toggle("timer-warning", totalSeconds <= 300 && totalSeconds > 60);
        timer.classList.toggle("timer-danger", totalSeconds <= 60);
        window.setTimeout(render, 1000);
    }

    if (form) {
        form.addEventListener("submit", (event) => {
            window.setTimeout(() => {
                if (!event.defaultPrevented) {
                    submitted = true;
                }
            }, 0);
        });
    }

    render();
});
