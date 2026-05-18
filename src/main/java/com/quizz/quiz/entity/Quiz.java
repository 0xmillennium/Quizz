package com.quizz.quiz.entity;

import com.quizz.category.entity.Category;
import com.quizz.common.entity.BaseEntity;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.question.entity.Question;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "quizzes")
public class Quiz extends BaseEntity {

    public static final int DEFAULT_ATTEMPT_LIMIT = 3;
    public static final int DEFAULT_RETAKE_COOLDOWN_MINUTES = 1440;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "question_count", nullable = false)
    private int questionCount;

    @Column(name = "attempt_limit", nullable = false)
    private int attemptLimit;

    @Column(name = "retake_cooldown_minutes", nullable = false)
    private int retakeCooldownMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private QuizStatus status;

    @OneToMany(
            mappedBy = "quiz",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("displayOrder ASC")
    private List<QuizQuestion> questions = new ArrayList<>();

    protected Quiz() {
    }

    private Quiz(
            String title,
            String description,
            Category category,
            int durationMinutes,
            int questionCount,
            int attemptLimit,
            int retakeCooldownMinutes,
            List<Question> selectedQuestions
    ) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.durationMinutes = durationMinutes;
        this.questionCount = questionCount;
        this.attemptLimit = attemptLimit;
        this.retakeCooldownMinutes = retakeCooldownMinutes;
        this.status = QuizStatus.DRAFT;
        replaceQuestions(selectedQuestions);
    }

    public static Quiz create(
            String title,
            String description,
            Category category,
            int durationMinutes,
            int questionCount,
            int attemptLimit,
            int retakeCooldownMinutes,
            List<Question> selectedQuestions
    ) {
        return new Quiz(
                title,
                description,
                category,
                durationMinutes,
                questionCount,
                attemptLimit,
                retakeCooldownMinutes,
                selectedQuestions
        );
    }

    public void updateDraft(
            String title,
            String description,
            Category category,
            int durationMinutes,
            int questionCount,
            int attemptLimit,
            int retakeCooldownMinutes,
            List<Question> selectedQuestions
    ) {
        ensureDraft();
        this.title = title;
        this.description = description;
        this.category = category;
        this.durationMinutes = durationMinutes;
        this.questionCount = questionCount;
        this.attemptLimit = attemptLimit;
        this.retakeCooldownMinutes = retakeCooldownMinutes;
        replaceQuestions(selectedQuestions);
    }

    public void publish() {
        ensureDraft();
        if (questions.isEmpty()) {
            throw new BusinessRuleException("Quiz must have at least one question.");
        }
        if (questionCount < 1 || questionCount > questions.size()) {
            throw new BusinessRuleException("Questions per attempt must be between 1 and the pool size.");
        }
        if (attemptLimit < 1) {
            throw new BusinessRuleException("Attempt limit must be at least 1.");
        }
        if (retakeCooldownMinutes < 1) {
            throw new BusinessRuleException("Retake cooldown must be at least 1 minute.");
        }
        status = QuizStatus.PUBLISHED;
    }

    public void archive() {
        status = QuizStatus.ARCHIVED;
    }

    public boolean isDraft() {
        return status == QuizStatus.DRAFT;
    }

    public boolean isPublished() {
        return status == QuizStatus.PUBLISHED;
    }

    public boolean isArchived() {
        return status == QuizStatus.ARCHIVED;
    }

    private void replaceQuestions(List<Question> selectedQuestions) {
        questions.clear();

        int displayOrder = 1;
        for (Question question : selectedQuestions) {
            questions.add(QuizQuestion.create(this, question, displayOrder));
            displayOrder++;
        }
    }

    private void ensureDraft() {
        if (!isDraft()) {
            throw new BusinessRuleException("Only draft quizzes can be modified.");
        }
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public int getAttemptLimit() {
        return attemptLimit;
    }

    public int getRetakeCooldownMinutes() {
        return retakeCooldownMinutes;
    }

    public QuizStatus getStatus() {
        return status;
    }

    public List<QuizQuestion> getQuestions() {
        return Collections.unmodifiableList(questions);
    }
}
