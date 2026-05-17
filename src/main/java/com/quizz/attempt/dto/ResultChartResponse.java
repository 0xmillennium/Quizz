package com.quizz.attempt.dto;

public record ResultChartResponse(
        int correctCount,
        int wrongCount,
        int unansweredCount
) {
}
