package com.quizz.attempt.dto;

import java.time.Instant;

public record AutosaveAnswerResponse(
        Long attemptId,
        Long attemptQuestionId,
        Long selectedOptionId,
        int answerRevision,
        String status,
        boolean saved,
        boolean stale,
        boolean autoSubmitted,
        String redirectUrl,
        Instant savedAt,
        Instant expiresAt
) {
}
