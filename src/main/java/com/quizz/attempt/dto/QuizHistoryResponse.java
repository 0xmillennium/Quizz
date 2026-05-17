package com.quizz.attempt.dto;

import java.time.Instant;

public record QuizHistoryResponse(
        Long attemptId,
        String quizTitle,
        String categoryName,
        String status,
        int totalQuestions,
        int correctCount,
        int scorePercentage,
        Instant startedAt,
        Instant submittedAt
) {
}
