package com.quizz.attempt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quizz.attempt.entity.AttemptQuestion;
import com.quizz.attempt.entity.QuizAttempt;
import com.quizz.attempt.scoring.ScoreCalculator;
import com.quizz.attempt.scoring.ScoreResult;
import com.quizz.category.entity.Category;
import com.quizz.question.entity.Question;
import com.quizz.quiz.entity.Quiz;
import com.quizz.user.entity.User;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultScoringServiceTest {

    @Mock
    private ScoreCalculator scoreCalculator;

    private DefaultScoringService service;

    @BeforeEach
    void setUp() {
        service = new DefaultScoringService(scoreCalculator);
    }

    @Test
    void scoreEvaluatesQuestionsAndReturnsCalculatorResult() throws Exception {
        User user = AttemptTestFactory.user(1L);
        Category category = AttemptTestFactory.category(2L, "Science");
        Question first = AttemptTestFactory.question(10L, "First?", category);
        Question second = AttemptTestFactory.question(20L, "Second?", category);
        Quiz quiz = AttemptTestFactory.quiz(3L, category, first, second);
        QuizAttempt attempt = AttemptTestFactory.attempt(4L, user, quiz, Instant.parse("2026-01-01T00:00:00Z"));
        AttemptQuestion firstAttemptQuestion = attempt.getQuestions().get(0);
        attempt.answerQuestion(firstAttemptQuestion.getId(), firstAttemptQuestion.getOptions().get(0).getId());
        ScoreResult scoreResult = new ScoreResult(2, 1, 0, 1, 50, "DEFAULT_V1");
        when(scoreCalculator.calculate(attempt.getQuestions())).thenReturn(scoreResult);

        ScoreResult result = service.score(attempt);

        assertThat(result).isSameAs(scoreResult);
        assertThat(firstAttemptQuestion.getCorrect()).isTrue();
        assertThat(attempt.getQuestions().get(1).getCorrect()).isFalse();
        verify(scoreCalculator).calculate(attempt.getQuestions());
    }
}
