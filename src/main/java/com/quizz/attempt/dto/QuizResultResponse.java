package com.quizz.attempt.dto;

import java.time.Instant;
import java.util.List;

public record QuizResultResponse(
        Long attemptId,
        String quizTitle,
        String categoryName,
        String status,
        int totalQuestions,
        int correctCount,
        int wrongCount,
        int unansweredCount,
        int scorePercentage,
        String scoringVersion,
        Instant startedAt,
        Instant expiresAt,
        Instant submittedAt,
        List<ResultQuestionResponse> questions
) {
}
