package com.quizz.attempt.service;

import com.quizz.attempt.dto.QuizResultResponse;
import com.quizz.attempt.dto.StartQuizResponse;
import com.quizz.attempt.dto.SubmitQuizRequest;

public interface QuizAttemptCommandService {

    StartQuizResponse startAttempt(Long quizId, Long userId);

    QuizResultResponse submitAttempt(
            Long attemptId,
            Long userId,
            SubmitQuizRequest request
    );

    void expireOverdueAttempts();
}
