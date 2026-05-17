package com.quizz.question.dto;

public record AnswerOptionResponse(
        Long id,
        String text,
        boolean correct,
        int displayOrder
) {
}
