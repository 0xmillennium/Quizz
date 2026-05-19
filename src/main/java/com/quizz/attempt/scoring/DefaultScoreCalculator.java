package com.quizz.attempt.scoring;

import com.quizz.attempt.entity.AttemptQuestion;
import com.quizz.attempt.entity.QuizAttempt;
import com.quizz.common.exception.BusinessRuleException;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Default percentage-based score calculator.
 *
 * <p>The calculator expects questions to have already been evaluated against
 * their attempt answer-option snapshots. It treats missing selections as
 * unanswered and derives wrong answers from the remaining evaluated questions.</p>
 */
@Component
public class DefaultScoreCalculator implements ScoreCalculator {

    @Override
    public ScoreResult calculate(List<AttemptQuestion> questions) {
        int totalQuestions = questions.size();
        if (totalQuestions == 0) {
            throw new BusinessRuleException("Cannot score an attempt with no questions.");
        }

        int unansweredCount = (int) questions.stream()
                .filter(question -> !question.isAnswered())
                .count();
        int correctCount = (int) questions.stream()
                .filter(AttemptQuestion::isCorrectlyAnswered)
                .count();
        int wrongCount = totalQuestions - correctCount - unansweredCount;
        int scorePercentage = (int) Math.round(correctCount * 100.0 / totalQuestions);

        return new ScoreResult(
                totalQuestions,
                correctCount,
                wrongCount,
                unansweredCount,
                scorePercentage,
                QuizAttempt.DEFAULT_SCORING_VERSION
        );
    }
}
