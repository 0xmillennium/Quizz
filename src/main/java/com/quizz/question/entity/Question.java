package com.quizz.question.entity;

import com.quizz.category.entity.Category;
import com.quizz.common.entity.BaseEntity;
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
@Table(name = "questions")
public class Question extends BaseEntity {

    @Column(name = "text", nullable = false, length = 1000)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private QuestionStatus status;

    @OneToMany(
            mappedBy = "question",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("displayOrder ASC")
    private List<AnswerOption> options = new ArrayList<>();

    protected Question() {
    }

    private Question(String text, Category category, List<AnswerOptionDraft> optionDrafts) {
        this.text = text;
        this.category = category;
        this.status = QuestionStatus.ACTIVE;
        replaceOptions(optionDrafts);
    }

    public static Question create(String text, Category category, List<AnswerOptionDraft> optionDrafts) {
        return new Question(text, category, optionDrafts);
    }

    public void update(String text, Category category, List<AnswerOptionDraft> optionDrafts) {
        this.text = text;
        this.category = category;
        replaceOptions(optionDrafts);
    }

    public void archive() {
        status = QuestionStatus.ARCHIVED;
    }

    public void restore() {
        status = QuestionStatus.ACTIVE;
    }

    public boolean isActive() {
        return status == QuestionStatus.ACTIVE;
    }

    private void replaceOptions(List<AnswerOptionDraft> optionDrafts) {
        options.clear();

        int displayOrder = 1;
        for (AnswerOptionDraft draft : optionDrafts) {
            options.add(AnswerOption.create(this, draft.text(), draft.correct(), displayOrder));
            displayOrder++;
        }
    }

    public String getText() {
        return text;
    }

    public Category getCategory() {
        return category;
    }

    public QuestionStatus getStatus() {
        return status;
    }

    public List<AnswerOption> getOptions() {
        return Collections.unmodifiableList(options);
    }
}
