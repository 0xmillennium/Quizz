package com.quizz.admin.dto;

import java.time.Instant;

public record AdminRecentAttemptResponse(
        Long attemptId,
        Long userId,
        String userFullName,
        String quizTitle,
        String categoryName,
        String status,
        String completionReason,
        int scorePercentage,
        Instant startedAt,
        Instant submittedAt,
        Instant abandonedAt) {
}
