package com.quizz.attempt.dto;

import java.time.Instant;

/**
 * Attempt history row for the authenticated user.
 *
 * <p>
 * The row summarizes lifecycle and score state from the attempt snapshot.
 * It is scoped to the current user by the query service and contains no answer
 * correctness details.
 * </p>
 */
public record QuizHistoryResponse(
        Long attemptId,
        String quizTitle,
        String categoryName,
        String status,
        String completionReason,
        int totalQuestions,
        int correctCount,
        int scorePercentage,
        Instant startedAt,
        Instant expiresAt,
        Instant submittedAt,
        Instant abandonedAt) {
}
