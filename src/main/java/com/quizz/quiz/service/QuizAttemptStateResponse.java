package com.quizz.quiz.service;

import java.time.Instant;

public record QuizAttemptStateResponse(
        Long activeAttemptId,
        Instant startedAt,
        Instant expiresAt,
        Long autoSubmittedAttemptId
) {
    public boolean hasActiveAttempt() {
        return activeAttemptId != null;
    }

    public boolean hasAutoSubmittedAttempt() {
        return autoSubmittedAttemptId != null;
    }
}
