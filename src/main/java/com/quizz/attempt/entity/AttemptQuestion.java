package com.quizz.attempt.entity;

import com.quizz.common.entity.BaseEntity;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.question.entity.Question;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(
        name = "attempt_questions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_attempt_questions_attempt_order",
                columnNames = {"attempt_id", "display_order"}
        )
)
public class AttemptQuestion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    @Column(name = "original_question_id")
    private Long originalQuestionId;

    @Column(name = "question_text", nullable = false, length = 1000)
    private String questionText;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "selected_option_id")
    private Long selectedOptionId;

    @Column(name = "correct")
    private Boolean correct;

    @OneToMany(
            mappedBy = "attemptQuestion",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("displayOrder ASC")
    private List<AttemptAnswerOption> options = new ArrayList<>();

    protected AttemptQuestion() {
    }

    private AttemptQuestion(QuizAttempt attempt, Question question, int displayOrder) {
        this.attempt = attempt;
        this.originalQuestionId = question.getId();
        this.questionText = question.getText();
        this.displayOrder = displayOrder;
        this.correct = null;
        question.getOptions().forEach(option -> options.add(AttemptAnswerOption.snapshotFrom(this, option)));
    }

    static AttemptQuestion snapshotFrom(
            QuizAttempt attempt,
            Question question,
            int displayOrder
    ) {
        return new AttemptQuestion(attempt, question, displayOrder);
    }

    void selectOption(Long selectedOptionId) {
        if (selectedOptionId == null) {
            this.selectedOptionId = null;
            return;
        }
        findOption(selectedOptionId);
        this.selectedOptionId = selectedOptionId;
    }

    void evaluate() {
        if (selectedOptionId == null) {
            correct = false;
            return;
        }
        correct = findOption(selectedOptionId).isCorrect();
    }

    public boolean isAnswered() {
        return selectedOptionId != null;
    }

    public boolean isCorrectlyAnswered() {
        return Boolean.TRUE.equals(correct);
    }

    private AttemptAnswerOption findOption(Long selectedOptionId) {
        return options.stream()
                .filter(option -> selectedOptionId.equals(option.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("Selected option does not belong to this question."));
    }

    public QuizAttempt getAttempt() {
        return attempt;
    }

    public Long getOriginalQuestionId() {
        return originalQuestionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public Long getSelectedOptionId() {
        return selectedOptionId;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public List<AttemptAnswerOption> getOptions() {
        return Collections.unmodifiableList(options);
    }
}
