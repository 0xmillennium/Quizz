package com.quizz.attempt.service;

import com.quizz.attempt.dto.ResultChartResponse;
import com.quizz.attempt.entity.QuizAttempt;
import java.util.List;

/**
 * Read boundary for attempt play, result, chart, and history views.
 *
 * <p>Implementations resolve attempts by user id for ownership and should not
 * mutate lifecycle state. Result and chart reads are valid for completed
 * attempts, while active play DTOs must not expose answer correctness.</p>
 */
public interface QuizAttemptQueryService {

    QuizAttempt getAttemptPage(Long attemptId, Long userId);

    QuizAttempt getResult(Long attemptId, Long userId);

    List<QuizAttempt> findHistoryByUser(Long userId);

    ResultChartResponse getResultChart(Long attemptId, Long userId);
}
