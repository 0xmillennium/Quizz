package com.quizz.attempt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quizz.attempt.dto.ResultChartResponse;
import com.quizz.attempt.entity.AttemptStatus;
import com.quizz.attempt.entity.QuizAttempt;
import com.quizz.attempt.mapper.QuizAttemptMapper;
import com.quizz.attempt.repository.QuizAttemptRepository;
import com.quizz.attempt.scoring.ScoreResult;
import com.quizz.category.entity.Category;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.question.entity.Question;
import com.quizz.quiz.entity.Quiz;
import com.quizz.user.entity.User;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultQuizAttemptQueryServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private QuizAttemptMapper quizAttemptMapper;

    private User user;
    private Quiz quiz;
    private DefaultQuizAttemptQueryService service;

    @BeforeEach
    void setUp() throws Exception {
        user = AttemptTestFactory.user(1L);
        Category category = AttemptTestFactory.category(2L, "Science");
        Question question = AttemptTestFactory.question(10L, "First?", category);
        quiz = AttemptTestFactory.quiz(3L, category, question);
        service = new DefaultQuizAttemptQueryService(
                quizAttemptRepository,
                quizAttemptMapper,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void getAttemptPageReturnsInProgressAttempt() throws Exception {
        QuizAttempt attempt = AttemptTestFactory.attempt(4L, user, quiz, NOW.minusSeconds(60));
        when(quizAttemptRepository.findByIdAndUserIdWithQuestionsAndOptions(4L, 1L)).thenReturn(Optional.of(attempt));

        assertThat(service.getAttemptPage(4L, 1L)).isSameAs(attempt);
    }

    @Test
    void getAttemptPageRejectsCompletedAttempt() throws Exception {
        QuizAttempt attempt = AttemptTestFactory.attempt(4L, user, quiz, NOW.minusSeconds(60));
        attempt.complete(NOW.minusSeconds(10), new ScoreResult(1, 0, 0, 1, 0, "DEFAULT_V1"));
        when(quizAttemptRepository.findByIdAndUserIdWithQuestionsAndOptions(4L, 1L)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> service.getAttemptPage(4L, 1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Attempt is not in progress.");
    }

    @Test
    void getAttemptPageRejectsExpiredStatusAttempt() throws Exception {
        QuizAttempt attempt = AttemptTestFactory.attempt(4L, user, quiz, NOW.minusSeconds(60));
        attempt.markExpired();
        when(quizAttemptRepository.findByIdAndUserIdWithQuestionsAndOptions(4L, 1L)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> service.getAttemptPage(4L, 1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Attempt is not in progress.");
    }

    @Test
    void getAttemptPageRejectsTimeExpiredAttemptWithoutMutating() throws Exception {
        QuizAttempt attempt = AttemptTestFactory.attempt(4L, user, quiz, NOW.minusSeconds(30 * 60));
        when(quizAttemptRepository.findByIdAndUserIdWithQuestionsAndOptions(4L, 1L)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> service.getAttemptPage(4L, 1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Attempt has expired.");
        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.IN_PROGRESS);
    }

    @Test
    void getResultReturnsCompletedAttempt() throws Exception {
        QuizAttempt attempt = AttemptTestFactory.attempt(4L, user, quiz, NOW.minusSeconds(60));
        attempt.complete(NOW.minusSeconds(10), new ScoreResult(1, 0, 0, 1, 0, "DEFAULT_V1"));
        when(quizAttemptRepository.findResultByIdAndUserId(4L, 1L)).thenReturn(Optional.of(attempt));

        assertThat(service.getResult(4L, 1L)).isSameAs(attempt);
    }

    @Test
    void getResultReturnsExpiredAttempt() throws Exception {
        QuizAttempt attempt = AttemptTestFactory.attempt(4L, user, quiz, NOW.minusSeconds(60));
        attempt.markExpired();
        when(quizAttemptRepository.findResultByIdAndUserId(4L, 1L)).thenReturn(Optional.of(attempt));

        assertThat(service.getResult(4L, 1L)).isSameAs(attempt);
    }

    @Test
    void getResultRejectsInProgressAttempt() throws Exception {
        QuizAttempt attempt = AttemptTestFactory.attempt(4L, user, quiz, NOW.minusSeconds(60));
        when(quizAttemptRepository.findResultByIdAndUserId(4L, 1L)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> service.getResult(4L, 1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Attempt is still in progress.");
    }

    @Test
    void findHistoryByUserDelegates() throws Exception {
        QuizAttempt attempt = AttemptTestFactory.attempt(4L, user, quiz, NOW.minusSeconds(60));
        when(quizAttemptRepository.findHistoryByUserId(1L)).thenReturn(List.of(attempt));

        assertThat(service.findHistoryByUser(1L)).containsExactly(attempt);
    }

    @Test
    void getResultChartUsesStoredCountsThroughMapper() throws Exception {
        QuizAttempt attempt = AttemptTestFactory.attempt(4L, user, quiz, NOW.minusSeconds(60));
        attempt.complete(NOW.minusSeconds(10), new ScoreResult(1, 0, 0, 1, 0, "DEFAULT_V1"));
        ResultChartResponse chart = new ResultChartResponse(0, 0, 1);
        when(quizAttemptRepository.findResultByIdAndUserId(4L, 1L)).thenReturn(Optional.of(attempt));
        when(quizAttemptMapper.toChartResponse(attempt)).thenReturn(chart);

        assertThat(service.getResultChart(4L, 1L)).isSameAs(chart);
        verify(quizAttemptMapper).toChartResponse(attempt);
    }
}
