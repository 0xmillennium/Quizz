package com.quizz.question.dto;

public record QuestionSummaryResponse(
        Long id,
        String text,
        String categoryName,
        String status,
        int optionCount
) {
}
