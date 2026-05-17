package com.quizz.admin.dto;

import java.util.List;

public record AdminResultQuestionResponse(
        Long attemptQuestionId,
        Long originalQuestionId,
        String questionText,
        int displayOrder,
        Long selectedOptionId,
        Boolean correct,
        List<AdminResultAnswerOptionResponse> options
) {
}
