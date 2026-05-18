package com.quizz.admin.dto;

import java.time.Instant;

public record AdminResultSummaryResponse(
        Long attemptId,
        Long userId,
        String userFullName,
        String quizTitle,
        String categoryName,
        String status,
        String completionReason,
        int totalQuestions,
        int correctCount,
        int wrongCount,
        int unansweredCount,
        int scorePercentage,
        Instant startedAt,
        Instant expiresAt,
        Instant submittedAt,
        Instant abandonedAt
) {
}
