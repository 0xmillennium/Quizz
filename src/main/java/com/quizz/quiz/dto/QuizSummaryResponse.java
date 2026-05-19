package com.quizz.quiz.dto;

public record QuizSummaryResponse(
        Long id,
        String title,
        String categoryName,
        int durationMinutes,
        int questionCount,
        int attemptLimit,
        int retakeCooldownMinutes) {
}
