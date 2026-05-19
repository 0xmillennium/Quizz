package com.quizz.attempt.dto;

import java.time.Instant;

/**
 * Result of one autosave command.
 *
 * <p>{@code saved} and {@code stale} distinguish accepted revisions from older
 * browser requests. {@code autoSubmitted} and {@code redirectUrl} indicate that
 * the attempt was overdue and completed with {@code TIME_EXPIRED} instead of
 * saving the incoming answer.</p>
 */
public record AutosaveAnswerResponse(
        Long attemptId,
        Long attemptQuestionId,
        Long selectedOptionId,
        int answerRevision,
        String status,
        boolean saved,
        boolean stale,
        boolean autoSubmitted,
        String redirectUrl,
        Instant savedAt,
        Instant expiresAt
) {
}
