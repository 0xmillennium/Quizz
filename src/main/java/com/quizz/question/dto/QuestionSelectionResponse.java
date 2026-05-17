package com.quizz.question.dto;

public record QuestionSelectionResponse(
        Long id,
        String text,
        String categoryName
) {
}
