package com.quizz.admin.dto;

import java.util.List;

/**
 * Administrator question snapshot within an attempt result.
 *
 * <p>
 * The record reflects the question text, selected snapshot option, and
 * correctness stored for the attempt. It does not read current live question
 * content.
 * </p>
 */
public record AdminResultQuestionResponse(
        Long attemptQuestionId,
        Long originalQuestionId,
        String questionText,
        int displayOrder,
        Long selectedOptionId,
        Boolean correct,
        List<AdminResultAnswerOptionResponse> options) {
}
