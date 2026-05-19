package com.quizz.attempt.service;

import com.quizz.attempt.entity.QuizAttempt;
import com.quizz.attempt.scoring.ScoreCalculator;
import com.quizz.attempt.scoring.ScoreResult;
import org.springframework.stereotype.Service;

/**
 * Scoring service that evaluates an attempt snapshot before calculation.
 *
 * <p>Evaluation writes per-question correctness onto the attempt snapshot so
 * result and reporting views can display stored scoring state consistently.</p>
 */
@Service
public class DefaultScoringService implements ScoringService {

    private final ScoreCalculator scoreCalculator;

    public DefaultScoringService(ScoreCalculator scoreCalculator) {
        this.scoreCalculator = scoreCalculator;
    }

    @Override
    public ScoreResult score(QuizAttempt attempt) {
        attempt.evaluateQuestions();
        return scoreCalculator.calculate(attempt.getQuestions());
    }
}
