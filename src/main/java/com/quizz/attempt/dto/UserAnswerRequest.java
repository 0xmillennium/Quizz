package com.quizz.attempt.dto;

import jakarta.validation.constraints.NotNull;

public class UserAnswerRequest {

    @NotNull
    private Long attemptQuestionId;

    private Long selectedOptionId;

    public Long getAttemptQuestionId() {
        return attemptQuestionId;
    }

    public void setAttemptQuestionId(Long attemptQuestionId) {
        this.attemptQuestionId = attemptQuestionId;
    }

    public Long getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(Long selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }
}
