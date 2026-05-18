package com.quizz.attempt.dto;

public record SubmitQuizResponse(
        Long attemptId,
        String completionReason
) {
}
