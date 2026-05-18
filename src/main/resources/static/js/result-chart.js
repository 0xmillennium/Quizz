document.addEventListener("DOMContentLoaded", () => {
    const container = document.getElementById("result-chart");
    const canvas = container ? container.querySelector("canvas") : null;

    if (!container || !canvas || !container.dataset.chartUrl) {
        return;
    }

    fetch(container.dataset.chartUrl)
        .then(response => response.json())
        .then(data => drawChart(canvas, [
            ["Correct", data.correctCount, cssColor("--color-success", "#16a34a")],
            ["Wrong", data.wrongCount, cssColor("--color-danger", "#dc2626")],
            ["Unanswered", data.unansweredCount, cssColor("--color-text-muted", "#64748b")]
        ]))
        .catch(() => {
            container.textContent = "Chart unavailable.";
        });
});

function cssColor(name, fallback) {
    const value = getComputedStyle(document.documentElement).getPropertyValue(name).trim();
    return value || fallback;
}

function drawChart(canvas, rows) {
    const context = canvas.getContext("2d");
    const width = canvas.width;
    const height = canvas.height;
    const labelWidth = 110;
    const barHeight = 34;
    const gap = 24;
    const max = Math.max(...rows.map(row => row[1]), 1);

    context.clearRect(0, 0, width, height);
    context.font = "14px Inter, Arial, sans-serif";
    context.textBaseline = "middle";

    rows.forEach((row, index) => {
        const [label, value, color] = row;
        const y = 36 + index * (barHeight + gap);
        const barWidth = Math.round((width - labelWidth - 40) * value / max);

        context.fillStyle = "#1f2933";
        context.fillText(label, 10, y + barHeight / 2);
        context.fillStyle = color;
        context.fillRect(labelWidth, y, barWidth, barHeight);
        context.fillStyle = "#1f2933";
        context.fillText(String(value), labelWidth + barWidth + 10, y + barHeight / 2);
    });
}
