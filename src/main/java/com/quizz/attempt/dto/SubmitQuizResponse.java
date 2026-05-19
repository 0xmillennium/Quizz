package com.quizz.attempt.dto;

/**
 * Terminal response for manual submit.
 *
 * <p>{@code completionReason} distinguishes a normal {@code MANUAL} submit
 * from an overdue submit resolved as {@code TIME_EXPIRED}.</p>
 */
public record SubmitQuizResponse(
        Long attemptId,
        String completionReason
) {
}
