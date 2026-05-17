package com.quizz.admin.dto;

public record AdminResultAnswerOptionResponse(
        Long attemptAnswerOptionId,
        Long originalAnswerOptionId,
        String optionText,
        boolean correct,
        boolean selected,
        int displayOrder
) {
}
