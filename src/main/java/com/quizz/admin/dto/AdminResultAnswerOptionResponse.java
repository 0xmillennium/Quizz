package com.quizz.admin.dto;

/**
 * Administrator answer-option snapshot within an attempt result.
 *
 * <p>Correctness is exposed here because admins review completed attempt
 * details. The values come from attempt answer-option snapshots rather than
 * live question-bank rows.</p>
 */
public record AdminResultAnswerOptionResponse(
        Long attemptAnswerOptionId,
        Long originalAnswerOptionId,
        String optionText,
        boolean correct,
        boolean selected,
        int displayOrder
) {
}
