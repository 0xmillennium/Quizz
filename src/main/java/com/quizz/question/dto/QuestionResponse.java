package com.quizz.question.dto;

import java.util.List;

public record QuestionResponse(
        Long id,
        String text,
        Long categoryId,
        String categoryName,
        String status,
        List<AnswerOptionResponse> options) {
}
