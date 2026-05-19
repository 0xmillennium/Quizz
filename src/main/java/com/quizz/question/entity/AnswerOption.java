package com.quizz.question.entity;

import com.quizz.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Owned answer option child of a {@link Question}.
 *
 * <p>
 * Answer options are managed through the question aggregate rather than a
 * separate repository or service. Correctness is copied into attempt snapshots
 * for scoring and reporting; active play-page DTOs must not expose it.
 * </p>
 */
@Entity
@Table(name = "answer_options", uniqueConstraints = @UniqueConstraint(name = "uk_answer_options_question_order", columnNames = {
        "question_id", "display_order"}))
public class AnswerOption extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "text", nullable = false, length = 500)
    private String text;

    @Column(name = "correct", nullable = false)
    private boolean correct;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected AnswerOption() {
    }

    private AnswerOption(Question question, String text, boolean correct, int displayOrder) {
        this.question = question;
        this.text = text;
        this.correct = correct;
        this.displayOrder = displayOrder;
    }

    static AnswerOption create(Question question, String text, boolean correct, int displayOrder) {
        return new AnswerOption(question, text, correct, displayOrder);
    }

    public Question getQuestion() {
        return question;
    }

    public String getText() {
        return text;
    }

    public boolean isCorrect() {
        return correct;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }
}
