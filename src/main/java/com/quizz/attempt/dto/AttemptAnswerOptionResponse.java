package com.quizz.attempt.dto;

public record AttemptAnswerOptionResponse(
        Long id,
        String optionText,
        int displayOrder
) {
}
