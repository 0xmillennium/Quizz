package com.quizz.attempt.entity;

import com.quizz.common.entity.BaseEntity;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.quiz.entity.Quiz;
import com.quizz.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.Instant;

/**
 * Attempt-rights window for a user and quiz pair.
 *
 * <p>
 * Starting or restarting an attempt consumes a right; resuming an active
 * attempt and completing an already-started attempt do not consume another
 * right. When rights are exhausted, cooldown begins only after no active
 * attempt remains. The repository uses pessimistic locking for command flows
 * that update this aggregate, and the version column preserves an additional
 * optimistic concurrency signal.
 * </p>
 */
@Entity
@Table(name = "quiz_attempt_allowances", uniqueConstraints = @UniqueConstraint(name = "uk_quiz_attempt_allowances_user_quiz", columnNames = {
        "user_id", "quiz_id"}))
public class QuizAttemptAllowance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "remaining_attempts", nullable = false)
    private int remainingAttempts;

    @Column(name = "cooldown_until")
    private Instant cooldownUntil;

    @Column(name = "last_consumed_at")
    private Instant lastConsumedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected QuizAttemptAllowance() {
    }

    private QuizAttemptAllowance(User user, Quiz quiz) {
        this.user = user;
        this.quiz = quiz;
        this.remainingAttempts = quiz.getAttemptLimit();
    }

    public static QuizAttemptAllowance initialize(User user, Quiz quiz) {
        return new QuizAttemptAllowance(user, quiz);
    }

    public void resetIfCooldownExpired(Quiz quiz, Instant now) {
        if (cooldownUntil != null && !now.isBefore(cooldownUntil)) {
            this.remainingAttempts = quiz.getAttemptLimit();
            this.cooldownUntil = null;
        }
    }

    public void consumeRight(Instant now) {
        if (cooldownUntil != null) {
            throw new BusinessRuleException("Quiz is in cooldown.");
        }
        if (remainingAttempts <= 0) {
            throw new BusinessRuleException("No attempts remaining.");
        }
        remainingAttempts--;
        lastConsumedAt = now;
    }

    public void startCooldown(Instant cooldownUntil) {
        this.remainingAttempts = 0;
        this.cooldownUntil = cooldownUntil;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public Instant getCooldownUntil() {
        return cooldownUntil;
    }

    public Instant getLastConsumedAt() {
        return lastConsumedAt;
    }

    public long getVersion() {
        return version;
    }
}
