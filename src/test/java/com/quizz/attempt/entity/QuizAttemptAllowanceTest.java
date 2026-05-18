package com.quizz.attempt.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.quizz.attempt.service.AttemptTestFactory;
import com.quizz.category.entity.Category;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.question.entity.Question;
import com.quizz.quiz.entity.Quiz;
import com.quizz.user.entity.User;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class QuizAttemptAllowanceTest {

    @Test
    void newAllowanceStartsWithQuizAttemptLimit() throws Exception {
        Quiz quiz = quizWithAttemptLimit(5);

        QuizAttemptAllowance allowance = QuizAttemptAllowance.initialize(user(), quiz);

        assertThat(allowance.getRemainingAttempts()).isEqualTo(5);
        assertThat(allowance.getCooldownUntil()).isNull();
    }

    @Test
    void consumeRightDoesNotGoNegative() throws Exception {
        Quiz quiz = quizWithAttemptLimit(1);
        QuizAttemptAllowance allowance = QuizAttemptAllowance.initialize(user(), quiz);
        allowance.consumeRight(Instant.parse("2026-01-01T10:00:00Z"));

        assertThatThrownBy(() -> allowance.consumeRight(Instant.parse("2026-01-01T10:01:00Z")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("No attempts remaining.");
        assertThat(allowance.getRemainingAttempts()).isZero();
    }

    @Test
    void resetIfCooldownExpiredRestoresQuizAttemptLimit() throws Exception {
        Quiz quiz = quizWithAttemptLimit(4);
        QuizAttemptAllowance allowance = QuizAttemptAllowance.initialize(user(), quiz);
        allowance.consumeRight(Instant.parse("2026-01-01T10:00:00Z"));
        allowance.startCooldown(Instant.parse("2026-01-01T11:00:00Z"));

        allowance.resetIfCooldownExpired(quiz, Instant.parse("2026-01-01T11:00:00Z"));

        assertThat(allowance.getRemainingAttempts()).isEqualTo(4);
        assertThat(allowance.getCooldownUntil()).isNull();
    }

    private User user() throws Exception {
        return AttemptTestFactory.user(1L);
    }

    private Quiz quizWithAttemptLimit(int attemptLimit) throws Exception {
        Category category = AttemptTestFactory.category(2L, "Science");
        Question question = AttemptTestFactory.question(3L, "Question?", category);
        Quiz quiz = Quiz.create("Science Quiz", null, category, 30, 1, attemptLimit, 60, java.util.List.of(question));
        AttemptTestFactory.setId(quiz, 4L);
        return quiz;
    }
}
