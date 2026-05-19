package com.quizz.attempt.dto;

import java.time.Instant;
import java.util.List;

/**
 * Completed attempt result model.
 *
 * <p>
 * Result DTOs may expose correctness because the attempt has reached a
 * terminal review context. Scores and question text come from the immutable
 * attempt snapshot, not from live quiz or question-bank state.
 * </p>
 */
public record QuizResultResponse(
        Long attemptId,
        String quizTitle,
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
        List<ResultQuestionResponse> questions) {
}
