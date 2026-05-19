package com.quizz.attempt.service;

import com.quizz.attempt.entity.AttemptAnswerOption;
import com.quizz.attempt.entity.AttemptQuestion;
import com.quizz.attempt.entity.QuizAttempt;
import com.quizz.category.entity.Category;
import com.quizz.common.entity.BaseEntity;
import com.quizz.question.entity.AnswerOptionDraft;
import com.quizz.question.entity.Question;
import com.quizz.quiz.entity.Quiz;
import com.quizz.user.entity.User;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public final class AttemptTestFactory {

    private AttemptTestFactory() {
    }

    public static User user(Long id) throws Exception {
        User user = User.createRegularUser("User", "user@example.com", "hash");
        setId(user, id);
        return user;
    }

    public static Category category(Long id, String name) throws Exception {
        Category category = Category.create(name, null);
        setId(category, id);
        return category;
    }

    public static Question question(Long id, String text, Category category) throws Exception {
        Question question = Question.create(
                text,
                category,
                List.of(
                        new AnswerOptionDraft(text + " correct", true),
                        new AnswerOptionDraft(text + " wrong", false)));
        setId(question, id);
        setId(question.getOptions().get(0), id * 10 + 1);
        setId(question.getOptions().get(1), id * 10 + 2);
        return question;
    }

    public static Quiz quiz(Long id, Category category, Question... questions) throws Exception {
        Quiz quiz = Quiz.create("Science Quiz", "Basics", category, 30, questions.length, 3, 1440, List.of(questions));
        quiz.publish();
        setId(quiz, id);
        return quiz;
    }

    public static QuizAttempt attempt(Long id, User user, Quiz quiz, Instant startedAt) throws Exception {
        QuizAttempt attempt = QuizAttempt.start(
                user,
                quiz,
                startedAt,
                quiz.getQuestions(),
                question -> question.getOptions().stream()
                        .sorted(Comparator.comparingInt(option -> option.getDisplayOrder()))
                        .toList());
        setId(attempt, id);
        long questionId = id * 100;
        long optionId = id * 1000;
        for (AttemptQuestion question : attempt.getQuestions()) {
            setId(question, questionId++);
            for (AttemptAnswerOption option : question.getOptions()) {
                setId(option, optionId++);
            }
        }
        return attempt;
    }

    public static void setId(BaseEntity entity, Long id) throws Exception {
        Field field = BaseEntity.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}
