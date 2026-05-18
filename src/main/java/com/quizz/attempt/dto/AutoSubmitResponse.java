package com.quizz.attempt.dto;

public record AutoSubmitResponse(
        Long attemptId,
        String status,
        String completionReason,
        String redirectUrl
) {
}
