package com.quizz.quiz.dto;

import java.util.List;

public record QuizAdminResponse(
        Long id,
        String title,
        String description,
        Long categoryId,
        String categoryName,
        int durationMinutes,
        int questionCount,
        int attemptLimit,
        int retakeCooldownMinutes,
        String status,
        int poolSize,
        List<QuizQuestionResponse> questions
) {
}
