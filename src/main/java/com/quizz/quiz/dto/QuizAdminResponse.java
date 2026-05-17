package com.quizz.quiz.dto;

import java.util.List;

public record QuizAdminResponse(
        Long id,
        String title,
        String description,
        Long categoryId,
        String categoryName,
        int durationMinutes,
        String status,
        int questionCount,
        List<QuizQuestionResponse> questions
) {
}
