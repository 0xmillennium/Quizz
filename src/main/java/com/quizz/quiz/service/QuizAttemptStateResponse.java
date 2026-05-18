package com.quizz.quiz.service;

import java.time.Instant;

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
