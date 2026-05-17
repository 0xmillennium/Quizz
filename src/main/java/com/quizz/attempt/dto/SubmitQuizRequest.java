package com.quizz.attempt.dto;

import java.util.ArrayList;
import java.util.List;

public class SubmitQuizRequest {

    private List<UserAnswerRequest> answers = new ArrayList<>();

    public List<UserAnswerRequest> getAnswers() {
        return answers;
    }

    public void setAnswers(List<UserAnswerRequest> answers) {
        this.answers = answers;
    }
}
