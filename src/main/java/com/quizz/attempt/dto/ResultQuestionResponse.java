package com.quizz.attempt.dto;

import java.util.List;

public record ResultQuestionResponse(
        Long id,
        String questionText,
        int displayOrder,
        Long selectedOptionId,
        boolean answered,
        boolean correct,
        List<ResultAnswerOptionResponse> options
) {
}
