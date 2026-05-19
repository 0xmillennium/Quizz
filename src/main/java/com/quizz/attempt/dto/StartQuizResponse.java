package com.quizz.attempt.dto;

import java.time.Instant;

/**
 * Response for start or restart commands.
 *
 * <p>
 * {@code resumed} means an existing active attempt was reused without
 * consuming a right. {@code previousAttemptAutoSubmitted} and
 * {@code previousAttemptId} report stale-overdue reconciliation, while
 * remaining rights and cooldown fields describe the allowance state after the
 * command.
 * </p>
 */
public record StartQuizResponse(
        Long attemptId,
        boolean resumed,
        boolean previousAttemptAutoSubmitted,
        Long previousAttemptId,
        int remainingAttempts,
        Instant cooldownUntil) {
}
