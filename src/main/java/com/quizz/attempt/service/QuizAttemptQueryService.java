package com.quizz.attempt.service;

import com.quizz.attempt.dto.ResultChartResponse;
import com.quizz.attempt.entity.QuizAttempt;
import java.util.List;

public interface QuizAttemptQueryService {

    QuizAttempt getAttemptPage(Long attemptId, Long userId);

    QuizAttempt getResult(Long attemptId, Long userId);

    List<QuizAttempt> findHistoryByUser(Long userId);

    ResultChartResponse getResultChart(Long attemptId, Long userId);
}
