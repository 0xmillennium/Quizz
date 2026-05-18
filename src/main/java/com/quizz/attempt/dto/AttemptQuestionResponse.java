package com.quizz.attempt.dto;

import java.util.List;

public record AttemptQuestionResponse(
        Long id,
        String questionText,
        int displayOrder,
        Long selectedOptionId,
        int answerRevision,
        List<AttemptAnswerOptionResponse> options
) {
}
