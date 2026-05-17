package com.quizz.attempt.dto;

import java.time.Instant;
import java.util.List;

public record QuizAttemptPageResponse(
        Long attemptId,
        String quizTitle,
        String categoryName,
        int durationMinutes,
        Instant startedAt,
        Instant expiresAt,
        List<AttemptQuestionResponse> questions
) {
}
