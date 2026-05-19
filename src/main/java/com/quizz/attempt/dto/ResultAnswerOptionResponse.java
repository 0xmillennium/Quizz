package com.quizz.attempt.dto;

public record ResultAnswerOptionResponse(
        Long id,
        String optionText,
        boolean correct,
        boolean selected,
        int displayOrder) {
}
