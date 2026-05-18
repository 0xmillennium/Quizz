package com.quizz.attempt.dto;

public record StartQuizResponse(
        Long attemptId,
        boolean resumed,
        boolean previousAttemptAutoSubmitted,
        Long previousAttemptId
) {
}
