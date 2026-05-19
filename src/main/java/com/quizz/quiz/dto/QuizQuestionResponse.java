package com.quizz.quiz.dto;

public record QuizQuestionResponse(
        Long questionId,
        String text,
        String categoryName,
        int displayOrder) {
}
