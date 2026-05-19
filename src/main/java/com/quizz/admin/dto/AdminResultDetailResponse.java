package com.quizz.admin.dto;

import java.time.Instant;
import java.util.List;

/**
 * Administrator detail model for one attempt snapshot.
 *
 * <p>The detail view is allowed to include correct-answer information because
 * it is an admin reporting context. Scores and answers come from persisted
 * attempt snapshot rows and are not recalculated for display. User email is not
 * part of this model.</p>
 */
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
