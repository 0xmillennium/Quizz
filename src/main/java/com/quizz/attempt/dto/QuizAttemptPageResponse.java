package com.quizz.attempt.dto;

import java.time.Instant;
import java.util.List;

/**
 * Active attempt play-page model.
 *
 * <p>The model is built from immutable attempt snapshots and contains timing,
 * question, and selected-answer state. It must remain free of correctness
 * fields; correctness appears only in result or admin reporting DTOs.</p>
 */
public record QuizAttemptPageResponse(
        Long attemptId,
        String quizTitle,
        String categoryName,
        int durationMinutes,
        Instant startedAt,
        Instant expiresAt,
        List<AttemptQuestionResponse> questions
) {
}
