package com.quizz.attempt.service;

import com.quizz.attempt.entity.QuizAttempt;
import com.quizz.attempt.scoring.ScoreResult;

/**
 * Scoring command for an attempt snapshot.
 *
 * <p>
 * Scoring evaluates immutable attempt questions and options, not live
 * question-bank records. A {@code null} selected option is treated as
 * unanswered, and correctness comes from snapshot answer options.
 * </p>
 */
public interface ScoringService {

    /**
     * Evaluates the attempt snapshot and returns aggregate score counters.
     */
    ScoreResult score(QuizAttempt attempt);
}
