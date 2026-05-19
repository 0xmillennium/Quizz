package com.quizz.attempt.dto;

/**
 * Answer-option snapshot rendered on the active play page.
 *
 * <p>The {@code id} is the attempt answer-option id needed for autosave. The
 * response intentionally excludes correctness and the original live answer
 * option id so active play cannot reveal the correct answer.</p>
 */
public record AttemptAnswerOptionResponse(
        Long id,
        String optionText,
        int displayOrder
) {
}
