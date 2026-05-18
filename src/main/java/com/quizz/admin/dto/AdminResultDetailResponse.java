package com.quizz.admin.dto;

import java.time.Instant;
import java.util.List;

public record AdminResultDetailResponse(
        Long attemptId,
        Long userId,
        String userFullName,
        String quizTitle,
        Long quizId,
        Long categoryId,
        String categoryName,
        String status,
        String completionReason,
        int totalQuestions,
        int correctCount,
        int wrongCount,
        int unansweredCount,
        int scorePercentage,
        String scoringVersion,
        Instant startedAt,
        Instant expiresAt,
        Instant submittedAt,
        Instant abandonedAt,
        List<AdminResultQuestionResponse> questions
) {
}
