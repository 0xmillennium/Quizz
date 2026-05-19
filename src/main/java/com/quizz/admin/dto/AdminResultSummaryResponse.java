package com.quizz.admin.dto;

import java.time.Instant;

/**
 * Administrator result-list row built from persisted attempt state.
 *
 * <p>
 * The summary reports snapshot quiz/category labels and stored score
 * counters without recalculating the result. It exposes user display names and
 * ids for admin lookup, not email addresses.
 * </p>
 */
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
        Instant abandonedAt) {
}
