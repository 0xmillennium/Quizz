package com.quizz.attempt.dto;

import java.util.List;

/**
 * Question snapshot rendered on the active play page.
 *
 * <p>
 * The response carries the current {@code selectedOptionId} and
 * {@code answerRevision} needed by resume and autosave. Correct-answer metadata
 * is intentionally absent from this play model.
 * </p>
 */
public record AttemptQuestionResponse(
        Long id,
        String questionText,
        int displayOrder,
        Long selectedOptionId,
        int answerRevision,
        List<AttemptAnswerOptionResponse> options) {
}
