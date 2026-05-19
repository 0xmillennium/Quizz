package com.quizz.attempt.scoring;

import com.quizz.attempt.entity.AttemptQuestion;
import java.util.List;

/**
 * Pure score calculator for evaluated attempt questions.
 *
 * <p>The calculator uses attempt snapshot state only. Unanswered questions have
 * {@code selectedOptionId == null}; answer correctness is taken from the
 * snapshot options captured when the attempt was created.</p>
 */
public interface ScoreCalculator {

    /**
     * Calculates score counters for an evaluated attempt-question list.
     */
    ScoreResult calculate(List<AttemptQuestion> questions);
}
