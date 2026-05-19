package com.quizz.attempt.entity;

import com.quizz.attempt.scoring.ScoreResult;
import com.quizz.common.entity.BaseEntity;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.question.entity.AnswerOption;
import com.quizz.question.entity.Question;
import com.quizz.quiz.entity.Quiz;
import com.quizz.quiz.entity.QuizQuestion;
import com.quizz.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Immutable quiz-content snapshot and lifecycle aggregate for one user's attempt.
 *
 * <p>
 * An attempt is {@code IN_PROGRESS}, {@code COMPLETED}, or {@code ABANDONED}.
 * Completed attempts have a completion reason of {@code MANUAL} or
 * {@code TIME_EXPIRED}; result counters are meaningful only after completion.
 * Restart abandons an in-progress attempt and creates a new attempt from the
 * same sampled snapshot rather than drawing a new pool.
 * </p>
 *
 * <p>
 * {@link AttemptQuestion} children store the question and answer-option
 * snapshot used for play, scoring, result review, and admin reporting. The live
 * question bank may change after an attempt starts without changing this
 * aggregate.
 * </p>
 */
@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt extends BaseEntity {

    public static final String DEFAULT_SCORING_VERSION = "DEFAULT_V1";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "quiz_title_snapshot", nullable = false, length = 150)
    private String quizTitleSnapshot;

    @Column(name = "category_id_snapshot", nullable = false)
    private Long categoryIdSnapshot;

    @Column(name = "category_name_snapshot", nullable = false, length = 80)
    private String categoryNameSnapshot;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "abandoned_at")
    private Instant abandonedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AttemptStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "completion_reason", length = 30)
    private AttemptCompletionReason completionReason;

    @Column(name = "total_questions", nullable = false)
    private int totalQuestions;

    @Column(name = "correct_count", nullable = false)
    private int correctCount;

    @Column(name = "wrong_count", nullable = false)
    private int wrongCount;

    @Column(name = "unanswered_count", nullable = false)
    private int unansweredCount;

    @Column(name = "score_percentage", nullable = false)
    private int scorePercentage;

    @Column(name = "scoring_version", nullable = false, length = 30)
    private String scoringVersion;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<AttemptQuestion> questions = new ArrayList<>();

    protected QuizAttempt() {
    }

    private QuizAttempt(
            User user,
            Quiz quiz,
            Instant startedAt,
            List<QuizQuestion> selectedQuestions,
            Function<Question, List<AnswerOption>> optionOrderProvider) {
        if (selectedQuestions.isEmpty()) {
            throw new BusinessRuleException("Quiz must have at least one question.");
        }
        this.user = user;
        this.quiz = quiz;
        this.quizTitleSnapshot = quiz.getTitle();
        this.categoryIdSnapshot = quiz.getCategory().getId();
        this.categoryNameSnapshot = quiz.getCategory().getName();
        this.durationMinutes = quiz.getDurationMinutes();
        this.startedAt = startedAt;
        this.expiresAt = startedAt.plus(Duration.ofMinutes(durationMinutes));
        this.status = AttemptStatus.IN_PROGRESS;
        this.totalQuestions = selectedQuestions.size();
        this.correctCount = 0;
        this.wrongCount = 0;
        this.unansweredCount = totalQuestions;
        this.scorePercentage = 0;
        this.scoringVersion = DEFAULT_SCORING_VERSION;
        int questionDisplayOrder = 1;
        for (QuizQuestion quizQuestion : selectedQuestions) {
            Question question = quizQuestion.getQuestion();
            questions.add(AttemptQuestion.snapshotFrom(
                    this,
                    question,
                    questionDisplayOrder,
                    optionOrderProvider.apply(question)));
            questionDisplayOrder++;
        }
    }

    private QuizAttempt(QuizAttempt source, Instant startedAt) {
        this.user = source.user;
        this.quiz = source.quiz;
        this.quizTitleSnapshot = source.quizTitleSnapshot;
        this.categoryIdSnapshot = source.categoryIdSnapshot;
        this.categoryNameSnapshot = source.categoryNameSnapshot;
        this.durationMinutes = source.durationMinutes;
        this.startedAt = startedAt;
        this.expiresAt = startedAt.plus(Duration.ofMinutes(durationMinutes));
        this.status = AttemptStatus.IN_PROGRESS;
        this.totalQuestions = source.totalQuestions;
        this.correctCount = 0;
        this.wrongCount = 0;
        this.unansweredCount = totalQuestions;
        this.scorePercentage = 0;
        this.scoringVersion = DEFAULT_SCORING_VERSION;
        source.questions.forEach(question -> questions.add(AttemptQuestion.copyForRestart(this, question)));
    }

    public static QuizAttempt start(
            User user,
            Quiz quiz,
            Instant startedAt,
            List<QuizQuestion> selectedQuestions,
            Function<Question, List<AnswerOption>> optionOrderProvider) {
        return new QuizAttempt(user, quiz, startedAt, selectedQuestions, optionOrderProvider);
    }

    public static QuizAttempt restartFromSnapshot(QuizAttempt source, Instant startedAt) {
        return new QuizAttempt(source, startedAt);
    }

    public boolean belongsTo(Long userId) {
        return userId != null && userId.equals(user.getId());
    }

    public boolean isInProgress() {
        return status == AttemptStatus.IN_PROGRESS;
    }

    public boolean isCompleted() {
        return status == AttemptStatus.COMPLETED;
    }

    public boolean isAbandoned() {
        return status == AttemptStatus.ABANDONED;
    }

    public boolean isOverdueAt(Instant now) {
        return !now.isBefore(this.expiresAt);
    }

    public void answerQuestion(Long attemptQuestionId, Long selectedOptionId) {
        ensureInProgress();
        findQuestion(attemptQuestionId).selectOption(selectedOptionId);
    }

    public void evaluateQuestions() {
        ensureInProgress();
        questions.forEach(AttemptQuestion::evaluate);
    }

    public void completeManually(Instant submittedAt, ScoreResult scoreResult) {
        ensureInProgress();
        this.submittedAt = submittedAt;
        this.abandonedAt = null;
        this.completionReason = AttemptCompletionReason.MANUAL;
        this.status = AttemptStatus.COMPLETED;
        applyScore(scoreResult);
    }

    /**
     * Completes this attempt because its server-side time window expired.
     *
     * <p>
     * The submitted time is set to {@code expiresAt}, not the current clock
     * time, because the logical submission moment is the end of the quiz window
     * even if the user returns later.
     * </p>
     */
    public void completeByTimeExpiry(ScoreResult scoreResult) {
        ensureInProgress();
        this.submittedAt = expiresAt;
        this.abandonedAt = null;
        this.completionReason = AttemptCompletionReason.TIME_EXPIRED;
        this.status = AttemptStatus.COMPLETED;
        applyScore(scoreResult);
    }

    /**
     * Abandons an active attempt as part of a restart transaction.
     *
     * <p>
     * The replacement attempt is created from this attempt's snapshot before
     * the old attempt becomes terminal at the service boundary, which prevents
     * restart from becoming a way to repeatedly sample the question pool.
     * </p>
     */
    public void abandonForRestart(Instant abandonedAt) {
        ensureInProgress();
        this.submittedAt = null;
        this.abandonedAt = abandonedAt;
        this.completionReason = null;
        this.status = AttemptStatus.ABANDONED;
    }

    private void applyScore(ScoreResult scoreResult) {
        this.totalQuestions = scoreResult.totalQuestions();
        this.correctCount = scoreResult.correctCount();
        this.wrongCount = scoreResult.wrongCount();
        this.unansweredCount = scoreResult.unansweredCount();
        this.scorePercentage = scoreResult.scorePercentage();
        this.scoringVersion = scoreResult.scoringVersion();
    }

    private AttemptQuestion findQuestion(Long attemptQuestionId) {
        return questions.stream()
                .filter(question -> attemptQuestionId != null && attemptQuestionId.equals(question.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("Attempt question not found."));
    }

    private void ensureInProgress() {
        if (!isInProgress()) {
            throw new BusinessRuleException("Attempt is not in progress.");
        }
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public User getUser() {
        return user;
    }

    public String getQuizTitleSnapshot() {
        return quizTitleSnapshot;
    }

    public Long getCategoryIdSnapshot() {
        return categoryIdSnapshot;
    }

    public String getCategoryNameSnapshot() {
        return categoryNameSnapshot;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Instant getAbandonedAt() {
        return abandonedAt;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public AttemptCompletionReason getCompletionReason() {
        return completionReason;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public int getWrongCount() {
        return wrongCount;
    }

    public int getUnansweredCount() {
        return unansweredCount;
    }

    public int getScorePercentage() {
        return scorePercentage;
    }

    public String getScoringVersion() {
        return scoringVersion;
    }

    public List<AttemptQuestion> getQuestions() {
        return Collections.unmodifiableList(questions);
    }
}
