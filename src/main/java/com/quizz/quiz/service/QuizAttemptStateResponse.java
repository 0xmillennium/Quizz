package com.quizz.quiz.service;

import java.time.Instant;

/**
 * User-specific attempt state shown on a quiz detail page.
 *
 * <p>The model includes active attempt timing, latest completed attempt,
 * remaining rights, cooldown, and any stale attempt auto-submitted while
 * resolving the page. Derived flags tell the view whether the user can start,
 * continue, or restart without exposing repository state.</p>
 */
public record QuizAttemptStateResponse(
        Long activeAttemptId,
        Instant activeStartedAt,
        Instant activeExpiresAt,
        Long latestCompletedAttemptId,
        Long autoSubmittedAttemptId,
        int attemptLimit,
        int remainingAttempts,
        Instant cooldownUntil,
        boolean canStart,
        boolean canContinue,
        boolean canRestart
) {
    public boolean hasActiveAttempt() {
        return activeAttemptId != null;
    }

    public boolean hasAutoSubmittedAttempt() {
        return autoSubmittedAttemptId != null;
    }

    public boolean isInCooldown() {
        return cooldownUntil != null;
    }
}
