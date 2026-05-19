package com.quizz.attempt.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.quizz.attempt.dto.AttemptAnswerOptionResponse;
import com.quizz.attempt.dto.QuizAttemptPageResponse;
import com.quizz.attempt.dto.QuizHistoryResponse;
import com.quizz.attempt.dto.QuizResultResponse;
import com.quizz.attempt.dto.ResultChartResponse;
import com.quizz.attempt.dto.SubmitQuizRequest;
import com.quizz.attempt.entity.AttemptQuestion;
import com.quizz.attempt.entity.QuizAttempt;
import com.quizz.attempt.scoring.ScoreResult;
import com.quizz.attempt.service.AttemptTestFactory;
import com.quizz.category.entity.Category;
import com.quizz.question.entity.Question;
import com.quizz.quiz.entity.Quiz;
import com.quizz.user.entity.User;
import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuizAttemptMapperTest {

    private QuizAttemptMapper mapper;
    private QuizAttempt attempt;

    @BeforeEach
    void setUp() throws Exception {
        mapper = new QuizAttemptMapper();
        User user = AttemptTestFactory.user(1L);
        Category category = AttemptTestFactory.category(2L, "Science");
        Question first = AttemptTestFactory.question(10L, "First?", category);
        Question second = AttemptTestFactory.question(20L, "Second?", category);
        Quiz quiz = AttemptTestFactory.quiz(3L, category, first, second);
        attempt = AttemptTestFactory.attempt(4L, user, quiz, Instant.parse("2026-01-01T12:00:00Z"));
    }

    @Test
    void toAttemptPageResponseMapsQuestionsAndOptionsWithoutCorrect() {
        QuizAttemptPageResponse response = mapper.toAttemptPageResponse(attempt);

        assertThat(response.attemptId()).isEqualTo(4L);
        assertThat(response.quizTitle()).isEqualTo("Science Quiz");
        assertThat(response.categoryName()).isEqualTo("Science");
        assertThat(response.questions()).hasSize(2);
        assertThat(response.questions().get(0).options()).hasSize(2);
        assertThat(response.questions().get(0).options().get(0).optionText()).isEqualTo("First? correct");
        assertThat(Arrays.stream(AttemptAnswerOptionResponse.class.getRecordComponents())
                .map(RecordComponent::getName))
                .containsExactly("id", "optionText", "displayOrder");
    }

    @Test
    void attemptAnswerOptionResponseHasNoCorrectProperty() {
        assertThat(Arrays.stream(AttemptAnswerOptionResponse.class.getRecordComponents())
                .map(RecordComponent::getName))
                .doesNotContain("correct", "isCorrect", "originalAnswerOptionId", "correctOptionText");
    }

    @Test
    void toSubmitQuizRequestCreatesOneAnswerPerQuestion() {
        SubmitQuizRequest request = mapper.toSubmitQuizRequest(attempt);

        assertThat(request.getAnswers()).hasSize(2);
        assertThat(request.getAnswers()).extracting("attemptQuestionId")
                .containsExactly(
                        attempt.getQuestions().get(0).getId(),
                        attempt.getQuestions().get(1).getId());
        assertThat(request.getAnswers()).allMatch(answer -> answer.getSelectedOptionId() == null);
    }

    @Test
    void toResultResponseMapsCorrectnessAndSelectedOption() {
        AttemptQuestion first = attempt.getQuestions().get(0);
        Long selectedOptionId = first.getOptions().get(0).getId();
        attempt.answerQuestion(first.getId(), selectedOptionId);
        attempt.evaluateQuestions();
        attempt.completeManually(Instant.parse("2026-01-01T12:01:00Z"), new ScoreResult(2, 1, 0, 1, 50, "DEFAULT_V1"));

        QuizResultResponse response = mapper.toResultResponse(attempt);

        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.questions().get(0).selectedOptionId()).isEqualTo(selectedOptionId);
        assertThat(response.questions().get(0).correct()).isTrue();
        assertThat(response.questions().get(0).options().get(0).correct()).isTrue();
        assertThat(response.questions().get(0).options().get(0).selected()).isTrue();
    }

    @Test
    void toHistoryResponseListMapsSummaryOnly() {
        List<QuizHistoryResponse> history = mapper.toHistoryResponseList(List.of(attempt));

        assertThat(history).hasSize(1);
        assertThat(history.get(0).attemptId()).isEqualTo(4L);
        assertThat(history.get(0).quizTitle()).isEqualTo("Science Quiz");
        assertThat(Arrays.stream(QuizHistoryResponse.class.getRecordComponents())
                .map(RecordComponent::getName))
                .doesNotContain("questions");
    }

    @Test
    void toChartResponseUsesStoredCounts() {
        attempt.evaluateQuestions();
        attempt.completeManually(Instant.parse("2026-01-01T12:01:00Z"), new ScoreResult(2, 0, 0, 2, 0, "DEFAULT_V1"));

        ResultChartResponse chart = mapper.toChartResponse(attempt);

        assertThat(chart.correctCount()).isZero();
        assertThat(chart.wrongCount()).isZero();
        assertThat(chart.unansweredCount()).isEqualTo(2);
    }
}
