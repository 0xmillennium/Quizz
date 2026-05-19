package com.quizz.quiz.dto;

import java.util.List;

public record QuizDetailResponse(
        Long id,
        String title,
        String description,
        String categoryName,
        int durationMinutes,
        int questionCount,
        int attemptLimit,
        int retakeCooldownMinutes,
        int poolSize,
        List<QuizQuestionResponse> questions) {
}
