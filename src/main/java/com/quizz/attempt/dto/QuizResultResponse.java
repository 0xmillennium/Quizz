package com.quizz.attempt.dto;

import java.time.Instant;
import java.util.List;

public record QuizResultResponse(
        Long attemptId,
        String quizTitle,
        String categoryName,
        String status,
        String completionReason,
        int totalQuestions,
        int correctCount,
        int wrongCount,
        int unansweredCount,
        int scorePercentage,
        String scoringVersion,
        Instant startedAt,
        Instant expiresAt,
        Instant submittedAt,
        Instant abandonedAt,
        List<ResultQuestionResponse> questions
) {
}
