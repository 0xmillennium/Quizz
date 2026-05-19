package com.quizz.attempt.dto;

/**
 * Response for an overdue auto-submit boundary.
 *
 * <p>The response reports the terminal attempt status and redirect target used
 * by MVC/AJAX callers after saved answers have been scored.</p>
 */
public record AutoSubmitResponse(
        Long attemptId,
        String status,
        String completionReason,
        String redirectUrl
) {
}
