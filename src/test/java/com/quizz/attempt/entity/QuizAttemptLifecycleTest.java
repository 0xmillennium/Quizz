package com.quizz.attempt.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.quizz.attempt.scoring.ScoreResult;
import com.quizz.attempt.service.AttemptTestFactory;
import com.quizz.category.entity.Category;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.question.entity.Question;
import com.quizz.quiz.entity.Quiz;
import com.quizz.user.entity.User;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuizAttemptLifecycleTest {

    private static final Instant STARTED_AT = Instant.parse("2026-01-01T12:00:00Z");
    private static final Instant NOW = Instant.parse("2026-01-01T12:05:00Z");
    private static final ScoreResult SCORE = new ScoreResult(1, 1, 0, 0, 100, "DEFAULT_V1");

    private QuizAttempt attempt;

    @BeforeEach
    void setUp() throws Exception {
        User user = AttemptTestFactory.user(1L);
        Category category = AttemptTestFactory.category(2L, "Science");
        Question question = AttemptTestFactory.question(10L, "First?", category);
        Quiz quiz = AttemptTestFactory.quiz(3L, category, question);
        attempt = AttemptTestFactory.attempt(4L, user, quiz, STARTED_AT);
    }

    @Test
    void completeManuallySetsCompletedManualAndSubmittedAtNow() {
        attempt.completeManually(NOW, SCORE);

        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.COMPLETED);
        assertThat(attempt.getCompletionReason()).isEqualTo(AttemptCompletionReason.MANUAL);
        assertThat(attempt.getSubmittedAt()).isEqualTo(NOW);
        assertThat(attempt.getAbandonedAt()).isNull();
    }

    @Test
    void completeByTimeExpirySetsCompletedTimeExpiredAndSubmittedAtExpiresAt() {
        attempt.completeByTimeExpiry(SCORE);

        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.COMPLETED);
        assertThat(attempt.getCompletionReason()).isEqualTo(AttemptCompletionReason.TIME_EXPIRED);
        assertThat(attempt.getSubmittedAt()).isEqualTo(attempt.getExpiresAt());
        assertThat(attempt.getAbandonedAt()).isNull();
    }

    @Test
    void abandonForRestartSetsAbandonedAndAbandonedAt() {
        attempt.abandonForRestart(NOW);

        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.ABANDONED);
        assertThat(attempt.getCompletionReason()).isNull();
        assertThat(attempt.getSubmittedAt()).isNull();
        assertThat(attempt.getAbandonedAt()).isEqualTo(NOW);
    }

    @Test
    void completedAttemptCannotBeAbandoned() {
        attempt.completeManually(NOW, SCORE);

        assertThatThrownBy(() -> attempt.abandonForRestart(NOW))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Attempt is not in progress.");
    }

    @Test
    void abandonedAttemptCannotBeCompleted() {
        attempt.abandonForRestart(NOW);

        assertThatThrownBy(() -> attempt.completeManually(NOW, SCORE))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Attempt is not in progress.");
        assertThatThrownBy(() -> attempt.completeByTimeExpiry(SCORE))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Attempt is not in progress.");
    }
}
