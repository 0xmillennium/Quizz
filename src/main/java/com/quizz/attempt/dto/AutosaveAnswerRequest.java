package com.quizz.attempt.dto;

public class AutosaveAnswerRequest {

    private Long selectedOptionId;
    private Integer answerRevision;

    public Long getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(Long selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }

    public Integer getAnswerRevision() {
        return answerRevision;
    }

    public void setAnswerRevision(Integer answerRevision) {
        this.answerRevision = answerRevision;
    }
}
