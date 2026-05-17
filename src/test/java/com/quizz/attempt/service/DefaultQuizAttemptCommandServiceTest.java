package com.quizz.attempt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quizz.attempt.dto.QuizResultResponse;
import com.quizz.attempt.dto.SubmitQuizRequest;
import com.quizz.attempt.dto.UserAnswerRequest;
import com.quizz.attempt.entity.AttemptQuestion;
import com.quizz.attempt.entity.AttemptStatus;
import com.quizz.attempt.entity.QuizAttempt;
import com.quizz.attempt.mapper.QuizAttemptMapper;
import com.quizz.attempt.repository.QuizAttemptRepository;
import com.quizz.attempt.scoring.ScoreResult;
import com.quizz.category.entity.Category;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.question.entity.Question;
import com.quizz.quiz.entity.Quiz;
import com.quizz.quiz.service.QuizQueryService;
import com.quizz.user.entity.User;
import com.quizz.user.service.UserQueryService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultQuizAttemptCommandServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private QuizQueryService quizQueryService;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private ScoringService scoringService;

    @Mock
    private QuizAttemptMapper quizAttemptMapper;

    private User user;
    private Quiz quiz;
    private DefaultQuizAttemptCommandService service;

    @BeforeEach
    void setUp() throws Exception {
        user = AttemptTestFactory.user(1L);
        Category category = AttemptTestFactory.category(2L, "Science");
        Question first = AttemptTestFactory.question(10L, "First?", category);
        Question second = AttemptTestFactory.question(20L, "Second?", category);
        quiz = AttemptTestFactory.quiz(3L, category, first, second);
        service = new DefaultQuizAttemptCommandService(
                quizAttemptRepository,
                quizQueryService,
                userQueryService,
                scoringService,
                quizAttemptMapper,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );

        lenient().when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(invocation -> {
            QuizAttempt attempt = invocation.getArgument(0);
            AttemptTestFactory.setId(attempt, 99L);
            return attempt;
        });
    }

    @Test
    void startAttemptCreatesInProgressAttempt() {
        stubStartDependencies();

        service.startAttempt(3L, 1L);

        ArgumentCaptor<QuizAttempt> captor = ArgumentCaptor.forClass(QuizAttempt.class);
        verify(quizAttemptRepository).save(captor.capture());
        QuizAttempt attempt = captor.getValue();
        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.IN_PROGRESS);
        assertThat(attempt.getStartedAt()).isEqualTo(NOW);
        assertThat(attempt.getExpiresAt()).isEqualTo(NOW.plusSeconds(30 * 60));
    }

    @Test
    void startAttemptSnapshotsQuizCategoryQuestionsAndOptions() {
        stubStartDependencies();

        service.startAttempt(3L, 1L);

        ArgumentCaptor<QuizAttempt> captor = ArgumentCaptor.forClass(QuizAttempt.class);
        verify(quizAttemptRepository).save(captor.capture());
        QuizAttempt attempt = captor.getValue();
        assertThat(attempt.getQuizTitleSnapshot()).isEqualTo("Science Quiz");
        assertThat(attempt.getCategoryIdSnapshot()).isEqualTo(2L);
        assertThat(attempt.getCategoryNameSnapshot()).isEqualTo("Science");
        assertThat(attempt.getQuestions()).extracting(AttemptQuestion::getQuestionText)
                .containsExactly("First?", "Second?");
        assertThat(attempt.getQuestions().get(0).getOriginalQuestionId()).isEqualTo(10L);
        assertThat(attempt.getQuestions().get(0).getOptions().get(0).getOriginalAnswerOptionId()).isEqualTo(101L);
        assertThat(attempt.getQuestions().get(0).getOptions().get(0).getOptionText()).isEqualTo("First? correct");
    }

    @Test
    void startAttemptRejectsExistingNonExpiredInProgressAttempt() throws Exception {
        stubStartDependencies();
        QuizAttempt existing = AttemptTestFactory.attempt(11L, user, quiz, NOW.minusSeconds(60));
        when(quizAttemptRepository.findByUserIdAndQuizIdAndStatus(1L, 3L, AttemptStatus.IN_PROGRESS))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.startAttempt(3L, 1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("You already have an active attempt for this quiz.");
    }

    @Test
    void startAttemptExpiresExistingExpiredInProgressAttemptAndCreatesNewAttempt() throws Exception {
        stubStartDependencies();
        QuizAttempt existing = AttemptTestFactory.attempt(11L, user, quiz, NOW.minusSeconds(31 * 60));
        when(quizAttemptRepository.findByUserIdAndQuizIdAndStatus(1L, 3L, AttemptStatus.IN_PROGRESS))
                .thenReturn(Optional.of(existing));

        service.startAttempt(3L, 1L);

        assertThat(existing.getStatus()).isEqualTo(AttemptStatus.EXPIRED);
        verify(quizAttemptRepository).flush();
        verify(quizAttemptRepository).save(any(QuizAttempt.class));
    }

    @Test
    void startAttemptUsesPublishedAttemptGraphAndUserQueryService() {
        stubStartDependencies();

        service.startAttempt(3L, 1L);

        verify(quizQueryService).getPublishedByIdForAttempt(3L);
        verify(userQueryService).getById(1L);
    }

    @Test
    void submitAttemptCompletesValidAttempt() throws Exception {
        QuizAttempt attempt = persistedAttempt();
        AttemptQuestion first = attempt.getQuestions().get(0);
        when(quizAttemptRepository.findByIdAndUserIdWithQuestionsAndOptions(4L, 1L)).thenReturn(Optional.of(attempt));
        ScoreResult scoreResult = new ScoreResult(2, 1, 1, 0, 50, "DEFAULT_V1");
        when(scoringService.score(attempt)).thenReturn(scoreResult);
        QuizResultResponse response = resultResponse(4L, "COMPLETED");
        when(quizAttemptMapper.toResultResponse(attempt)).thenReturn(response);

        QuizResultResponse result = service.submitAttempt(4L, 1L, request(answer(first, first.getOptions().get(0).getId())));

        assertThat(result).isSameAs(response);
        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.COMPLETED);
        assertThat(attempt.getSubmittedAt()).isEqualTo(NOW);
        assertThat(attempt.getScorePercentage()).isEqualTo(50);
    }

    @Test
    void submitAttemptStoresSelectedOptionIds() throws Exception {
        QuizAttempt attempt = persistedAttempt();
        AttemptQuestion first = attempt.getQuestions().get(0);
        Long selectedOptionId = first.getOptions().get(1).getId();
        when(quizAttemptRepository.findByIdAndUserIdWithQuestionsAndOptions(4L, 1L)).thenReturn(Optional.of(attempt));
        when(scoringService.score(attempt)).thenReturn(new ScoreResult(2, 0, 1, 1, 0, "DEFAULT_V1"));
        when(quizAttemptMapper.toResultResponse(attempt)).thenReturn(resultResponse(4L, "COMPLETED"));

        service.submitAttempt(4L, 1L, request(answer(first, selectedOptionId)));

        assertThat(first.getSelectedOptionId()).isEqualTo(selectedOptionId);
    }

    @Test
    void submitAttemptTreatsMissingAnswersAsUnanswered() throws Exception {
        QuizAttempt attempt = persistedAttempt();
        when(quizAttemptRepository.findByIdAndUserIdWithQuestionsAndOptions(4L, 1L)).thenReturn(Optional.of(attempt));
        when(scoringService.score(attempt)).thenReturn(new ScoreResult(2, 0, 0, 2, 0, "DEFAULT_V1"));
        when(quizAttemptMapper.toResultResponse(attempt)).thenReturn(resultResponse(4L, "COMPLETED"));

        service.submitAttempt(4L, 1L, request());

        assertThat(attempt.getQuestions()).allMatch(question -> question.getSelectedOptionId() == null);
        assertThat(attempt.getUnansweredCount()).isEqualTo(2);
    }

    @Test
    void submitAttemptRejectsDuplicateAttemptQuestionId() throws Exception {
        QuizAttempt attempt = persistedAttempt();
        AttemptQuestion first = attempt.getQuestions().get(0);
        when(quizAttemptRepository.findByIdAndUserIdWithQuestionsAndOptions(4L, 1L)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> service.submitAttempt(4L, 1L, request(
                answer(first, first.getOptions().get(0).getId()),
                answer(first, first.getOptions().get(1).getId())
        )))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Duplicate answers are not allowed.");
    }

    @Test
    void submitAttemptRejectsUnknownAttemptQuestionId() throws Exception {
        QuizAttempt attempt = persistedAttempt();
        when(quizAttemptRepository.findByIdAndUserIdWithQuestionsAndOptions(4L, 1L)).thenReturn(Optional.of(attempt));

        UserAnswerRequest answer = new UserAnswerRequest();
        answer.setAttemptQuestionId(999L);

        assertThatThrownBy(() -> service.submitAttempt(4L, 1L, request(answer)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Attempt question not found.");
    }

    @Test
    void submitAttemptRejectsOptionFromAnotherQuestion() throws Exception {
        QuizAttempt attempt = persistedAttempt();
        AttemptQuestion first = attempt.getQuestions().get(0);
        AttemptQuestion second = attempt.getQuestions().get(1);
        when(quizAttemptRepository.findByIdAndUserIdWithQuestionsAndOptions(4L, 1L)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> service.submitAttempt(
                4L,
                1L,
                request(answer(first, second.getOptions().get(0).getId()))
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Selected option does not belong to this question.");
    }

    @Test
    void submitAttemptMarksExpiredIfNowIsAtOrAfterExpiresAt() throws Exception {
        QuizAttempt attempt = AttemptTestFactory.attempt(4L, user, quiz, NOW.minusSeconds(30 * 60));
        when(quizAttemptRepository.findByIdAndUserIdWithQuestionsAndOptions(4L, 1L)).thenReturn(Optional.of(attempt));
        QuizResultResponse response = resultResponse(4L, "EXPIRED");
        when(quizAttemptMapper.toResultResponse(attempt)).thenReturn(response);

        QuizResultResponse result = service.submitAttempt(4L, 1L, request());

        assertThat(result).isSameAs(response);
        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.EXPIRED);
    }

    @Test
    void submitAttemptDoesNotScoreExpiredAttempt() throws Exception {
        QuizAttempt attempt = AttemptTestFactory.attempt(4L, user, quiz, NOW.minusSeconds(30 * 60));
        when(quizAttemptRepository.findByIdAndUserIdWithQuestionsAndOptions(4L, 1L)).thenReturn(Optional.of(attempt));
        when(quizAttemptMapper.toResultResponse(attempt)).thenReturn(resultResponse(4L, "EXPIRED"));

        service.submitAttempt(4L, 1L, request());

        verify(scoringService, never()).score(any());
        assertThat(attempt.getScorePercentage()).isZero();
    }

    @Test
    void submitAttemptRejectsCompletedAttempt() throws Exception {
        QuizAttempt attempt = persistedAttempt();
        attempt.complete(NOW.minusSeconds(10), new ScoreResult(2, 1, 1, 0, 50, "DEFAULT_V1"));
        when(quizAttemptRepository.findByIdAndUserIdWithQuestionsAndOptions(4L, 1L)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> service.submitAttempt(4L, 1L, request()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Attempt is not in progress.");
    }

    @Test
    void submitAttemptRejectsAlreadyExpiredAttempt() throws Exception {
        QuizAttempt attempt = persistedAttempt();
        attempt.markExpired();
        when(quizAttemptRepository.findByIdAndUserIdWithQuestionsAndOptions(4L, 1L)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> service.submitAttempt(4L, 1L, request()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Attempt is not in progress.");
    }

    @Test
    void expireOverdueAttemptsMarksExpiredAttempts() throws Exception {
        QuizAttempt first = persistedAttempt();
        QuizAttempt second = AttemptTestFactory.attempt(5L, user, quiz, NOW.minusSeconds(31 * 60));
        when(quizAttemptRepository.findByStatusAndExpiresAtBeforeOrAt(AttemptStatus.IN_PROGRESS, NOW))
                .thenReturn(List.of(first, second));

        service.expireOverdueAttempts();

        assertThat(first.getStatus()).isEqualTo(AttemptStatus.EXPIRED);
        assertThat(second.getStatus()).isEqualTo(AttemptStatus.EXPIRED);
    }

    private void stubStartDependencies() {
        when(userQueryService.getById(1L)).thenReturn(user);
        when(quizQueryService.getPublishedByIdForAttempt(3L)).thenReturn(quiz);
        when(quizAttemptRepository.findByUserIdAndQuizIdAndStatus(1L, 3L, AttemptStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
    }

    private QuizAttempt persistedAttempt() throws Exception {
        return AttemptTestFactory.attempt(4L, user, quiz, NOW.minusSeconds(60));
    }

    private SubmitQuizRequest request(UserAnswerRequest... answers) {
        SubmitQuizRequest request = new SubmitQuizRequest();
        request.setAnswers(List.of(answers));
        return request;
    }

    private UserAnswerRequest answer(AttemptQuestion question, Long selectedOptionId) {
        UserAnswerRequest answer = new UserAnswerRequest();
        answer.setAttemptQuestionId(question.getId());
        answer.setSelectedOptionId(selectedOptionId);
        return answer;
    }

    private QuizResultResponse resultResponse(Long attemptId, String status) {
        return new QuizResultResponse(
                attemptId,
                "Science Quiz",
                "Science",
                status,
                2,
                0,
                0,
                2,
                0,
                "DEFAULT_V1",
                NOW.minusSeconds(60),
                NOW.plusSeconds(60),
                null,
                List.of()
        );
    }
}
