package com.quizz.attempt.service;

import com.quizz.attempt.dto.AutoSubmitResponse;
import com.quizz.attempt.dto.AutosaveAnswerResponse;
import com.quizz.attempt.dto.StartQuizResponse;
import com.quizz.attempt.dto.SubmitQuizRequest;
import com.quizz.attempt.dto.SubmitQuizResponse;

public interface QuizAttemptCommandService {

    StartQuizResponse startAttempt(Long quizId, Long userId);

    StartQuizResponse restartAttempt(Long attemptId, Long userId);

    AutosaveAnswerResponse autosaveAnswer(
            Long attemptId,
            Long attemptQuestionId,
            Long userId,
            Long selectedOptionId,
            int answerRevision
    );

    AutoSubmitResponse autoSubmitIfOverdue(Long attemptId, Long userId);

    int autoSubmitOverdueAttemptsForUser(Long userId);

    SubmitQuizResponse submitAttempt(
            Long attemptId,
            Long userId,
            SubmitQuizRequest request
    );
}
