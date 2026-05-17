package com.quizz.attempt.dto;

import java.util.List;

public record AttemptQuestionResponse(
        Long id,
        String questionText,
        int displayOrder,
        List<AttemptAnswerOptionResponse> options
) {
}
