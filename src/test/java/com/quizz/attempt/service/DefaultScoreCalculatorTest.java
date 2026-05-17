package com.quizz.attempt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.quizz.attempt.entity.AttemptQuestion;
import com.quizz.attempt.entity.QuizAttempt;
import com.quizz.attempt.scoring.DefaultScoreCalculator;
import com.quizz.attempt.scoring.ScoreResult;
import com.quizz.category.entity.Category;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.question.entity.Question;
import com.quizz.quiz.entity.Quiz;
import com.quizz.user.entity.User;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultScoreCalculatorTest {

    private DefaultScoreCalculator calculator;
    private User user;
    private Quiz quiz;

    @BeforeEach
    void setUp() throws Exception {
        calculator = new DefaultScoreCalculator();
        Category category = AttemptTestFactory.category(1L, "Science");
        user = AttemptTestFactory.user(2L);
        Question first = AttemptTestFactory.question(10L, "First?", category);
        Question second = AttemptTestFactory.question(20L, "Second?", category);
        Question third = AttemptTestFactory.question(30L, "Third?", category);
        quiz = AttemptTestFactory.quiz(3L, category, first, second, third);
    }

    @Test
    void allCorrectScoresOneHundred() throws Exception {
        QuizAttempt attempt = AttemptTestFactory.attempt(1L, user, quiz, Instant.parse("2026-01-01T00:00:00Z"));
        answer(attempt, 0, true);
        answer(attempt, 1, true);
        answer(attempt, 2, true);
        attempt.evaluateQuestions();

        ScoreResult result = calculator.calculate(attempt.getQuestions());

        assertThat(result.correctCount()).isEqualTo(3);
        assertThat(result.scorePercentage()).isEqualTo(100);
    }

    @Test
    void allWrongScoresZero() throws Exception {
        QuizAttempt attempt = AttemptTestFactory.attempt(1L, user, quiz, Instant.parse("2026-01-01T00:00:00Z"));
        answer(attempt, 0, false);
        answer(attempt, 1, false);
        answer(attempt, 2, false);
        attempt.evaluateQuestions();

        ScoreResult result = calculator.calculate(attempt.getQuestions());

        assertThat(result.correctCount()).isZero();
        assertThat(result.wrongCount()).isEqualTo(3);
        assertThat(result.scorePercentage()).isZero();
    }

    @Test
    void unansweredQuestionsAreCounted() throws Exception {
        QuizAttempt attempt = AttemptTestFactory.attempt(1L, user, quiz, Instant.parse("2026-01-01T00:00:00Z"));
        answer(attempt, 0, true);
        attempt.evaluateQuestions();

        ScoreResult result = calculator.calculate(attempt.getQuestions());

        assertThat(result.correctCount()).isEqualTo(1);
        assertThat(result.unansweredCount()).isEqualTo(2);
        assertThat(result.wrongCount()).isZero();
    }

    @Test
    void mixedScoreRounds() throws Exception {
        QuizAttempt attempt = AttemptTestFactory.attempt(1L, user, quiz, Instant.parse("2026-01-01T00:00:00Z"));
        answer(attempt, 0, true);
        answer(attempt, 1, false);
        attempt.evaluateQuestions();

        ScoreResult result = calculator.calculate(attempt.getQuestions());

        assertThat(result.scorePercentage()).isEqualTo(33);
    }

    @Test
    void scoringVersionIsDefaultV1() throws Exception {
        QuizAttempt attempt = AttemptTestFactory.attempt(1L, user, quiz, Instant.parse("2026-01-01T00:00:00Z"));
        attempt.evaluateQuestions();

        assertThat(calculator.calculate(attempt.getQuestions()).scoringVersion()).isEqualTo("DEFAULT_V1");
    }

    @Test
    void zeroQuestionsThrowsBusinessRuleException() {
        assertThatThrownBy(() -> calculator.calculate(List.of()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Cannot score an attempt with no questions.");
    }

    private void answer(QuizAttempt attempt, int questionIndex, boolean correct) {
        AttemptQuestion question = attempt.getQuestions().get(questionIndex);
        Long selectedOptionId = question.getOptions().stream()
                .filter(option -> option.isCorrect() == correct)
                .findFirst()
                .orElseThrow()
                .getId();
        attempt.answerQuestion(question.getId(), selectedOptionId);
    }
}
