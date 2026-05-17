package com.quizz.quiz.entity;

import com.quizz.common.entity.BaseEntity;
import com.quizz.question.entity.Question;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "quiz_questions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_quiz_questions_quiz_question",
                        columnNames = {"quiz_id", "question_id"}
                ),
                @UniqueConstraint(
                        name = "uk_quiz_questions_quiz_order",
                        columnNames = {"quiz_id", "display_order"}
                )
        }
)
public class QuizQuestion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected QuizQuestion() {
    }

    private QuizQuestion(Quiz quiz, Question question, int displayOrder) {
        this.quiz = quiz;
        this.question = question;
        this.displayOrder = displayOrder;
    }

    static QuizQuestion create(Quiz quiz, Question question, int displayOrder) {
        return new QuizQuestion(quiz, question, displayOrder);
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public Question getQuestion() {
        return question;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }
}
