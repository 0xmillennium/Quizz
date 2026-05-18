package com.quizz.attempt.dto;

import java.time.Instant;

public record StartQuizResponse(
        Long attemptId,
        boolean resumed,
        boolean previousAttemptAutoSubmitted,
        Long previousAttemptId,
        int remainingAttempts,
        Instant cooldownUntil
) {
}
