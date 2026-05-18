package com.quizz.attempt.entity;

import com.quizz.common.entity.BaseEntity;
import com.quizz.question.entity.AnswerOption;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "attempt_answer_options",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_attempt_answer_options_question_order",
                columnNames = {"attempt_question_id", "display_order"}
        )
)
public class AttemptAnswerOption extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_question_id", nullable = false)
    private AttemptQuestion attemptQuestion;

    @Column(name = "original_answer_option_id")
    private Long originalAnswerOptionId;

    @Column(name = "option_text", nullable = false, length = 500)
    private String optionText;

    @Column(name = "correct", nullable = false)
    private boolean correct;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected AttemptAnswerOption() {
    }

    private AttemptAnswerOption(AttemptQuestion attemptQuestion, AnswerOption answerOption, int displayOrder) {
        this.attemptQuestion = attemptQuestion;
        this.originalAnswerOptionId = answerOption.getId();
        this.optionText = answerOption.getText();
        this.correct = answerOption.isCorrect();
        this.displayOrder = displayOrder;
    }

    private AttemptAnswerOption(AttemptQuestion attemptQuestion, AttemptAnswerOption source) {
        this.attemptQuestion = attemptQuestion;
        this.originalAnswerOptionId = source.originalAnswerOptionId;
        this.optionText = source.optionText;
        this.correct = source.correct;
        this.displayOrder = source.displayOrder;
    }

    static AttemptAnswerOption snapshotFrom(
            AttemptQuestion attemptQuestion,
            AnswerOption answerOption,
            int displayOrder
    ) {
        return new AttemptAnswerOption(attemptQuestion, answerOption, displayOrder);
    }

    static AttemptAnswerOption copyForRestart(AttemptQuestion attemptQuestion, AttemptAnswerOption source) {
        return new AttemptAnswerOption(attemptQuestion, source);
    }

    public AttemptQuestion getAttemptQuestion() {
        return attemptQuestion;
    }

    public Long getOriginalAnswerOptionId() {
        return originalAnswerOptionId;
    }

    public String getOptionText() {
        return optionText;
    }

    public boolean isCorrect() {
        return correct;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }
}
