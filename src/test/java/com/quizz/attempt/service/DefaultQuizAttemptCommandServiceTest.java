package com.quizz.attempt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quizz.attempt.dto.AutosaveAnswerResponse;
import com.quizz.attempt.dto.StartQuizResponse;
import com.quizz.attempt.dto.SubmitQuizRequest;
import com.quizz.attempt.dto.UserAnswerRequest;
import com.quizz.attempt.entity.AttemptCompletionReason;
import com.quizz.attempt.entity.AttemptQuestion;
import com.quizz.attempt.entity.AttemptStatus;
import com.quizz.attempt.entity.QuizAttempt;
import com.quizz.attempt.entity.QuizAttemptAllowance;
import com.quizz.attempt.random.AttemptRandomizer;
import com.quizz.attempt.repository.QuizAttemptAllowanceRepository;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultQuizAttemptCommandServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");
    private static final ScoreResult SCORE = new ScoreResult(2, 1, 1, 0, 50, "DEFAULT_V1");

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private QuizAttemptAllowanceRepository allowanceRepository;

    @Mock
    private QuizQueryService quizQueryService;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private ScoringService scoringService;

    private final AttemptRandomizer deterministicRandomizer = new AttemptRandomizer() {
        @Override
        public <T> List<T> shuffledCopy(List<T> source) {
            return new ArrayList<>(source);
        }
    };

    private User user;
    private Quiz quiz;
    private QuizAttemptAllowance allowance;
    private DefaultQuizAttemptCommandService service;

    @BeforeEach
    void setUp() throws Exception {
        user = AttemptTestFactory.user(1L);
        Category category = AttemptTestFactory.category(2L, "Science");
        Question first = AttemptTestFactory.question(10L, "First?", category);
        Question second = AttemptTestFactory.question(20L, "Second?", category);
        quiz = AttemptTestFactory.quiz(3L, category, first, second);
        allowance = QuizAttemptAllowance.initialize(user, quiz);
        service = new DefaultQuizAttemptCommandService(
                quizAttemptRepository,
                allowanceRepository,
                quizQueryService,
                userQueryService,
                scoringService,
                deterministicRandomizer,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );

        lenient().when(allowanceRepository.findByUserIdAndQuizIdForUpdate(1L, 3L))
                .thenReturn(Optional.of(allowance));
        lenient().when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(invocation -> {
            QuizAttempt attempt = invocation.getArgument(0);
            AttemptTestFactory.setId(attempt, 99L);
            return attempt;
        });
    }

    @Test
    void startAttemptCreatesNewAttemptWhenNoActiveAttempt() {
        stubStartDependencies(Optional.empty());

        StartQuizResponse response = service.startAttempt(3L, 1L);

        assertThat(response.attemptId()).isEqualTo(99L);
        assertThat(response.resumed()).isFalse();
        assertThat(response.remainingAttempts()).isEqualTo(2);
        assertThat(allowance.getRemainingAttempts()).isEqualTo(2);
        verify(quizAttemptRepository).save(any(QuizAttempt.class));
    }

    @Test
    void startAttemptResumesExistingActiveAttempt() throws Exception {
        QuizAttempt existing = AttemptTestFactory.attempt(11L, user, quiz, NOW.minusSeconds(60));
        stubStartDependencies(Optional.of(existing));

        StartQuizResponse response = service.startAttempt(3L, 1L);

        assertThat(response.attemptId()).isEqualTo(11L);
        assertThat(response.resumed()).isTrue();
        assertThat(allowance.getRemainingAttempts()).isEqualTo(3);
    }

    @Test
    void startAttemptAutoSubmitsOverdueActiveAttemptAndCreatesNewAttempt() throws Exception {
        QuizAttempt existing = AttemptTestFactory.attempt(11L, user, quiz, NOW.minusSeconds(31 * 60));
        stubStartDependencies(Optional.of(existing));
        when(scoringService.score(existing)).thenReturn(SCORE);

        StartQuizResponse response = service.startAttempt(3L, 1L);

        assertThat(existing.getStatus()).isEqualTo(AttemptStatus.COMPLETED);
        assertThat(existing.getCompletionReason()).isEqualTo(AttemptCompletionReason.TIME_EXPIRED);
        assertThat(existing.getSubmittedAt()).isEqualTo(existing.getExpiresAt());
        assertThat(response.previousAttemptAutoSubmitted()).isTrue();
        assertThat(response.previousAttemptId()).isEqualTo(11L);
    }

    @Test
    void restartAttemptAbandonsActiveAttemptAndCreatesNewAttempt() throws Exception {
        QuizAttempt existing = persistedAttempt();
        when(quizAttemptRepository.findByIdAndUserIdWithQuestions(4L, 1L)).thenReturn(Optional.of(existing));

        StartQuizResponse response = service.restartAttempt(4L, 1L);

        assertThat(existing.getStatus()).isEqualTo(AttemptStatus.ABANDONED);
        assertThat(existing.getAbandonedAt()).isEqualTo(NOW);
        assertThat(response.attemptId()).isEqualTo(99L);
        assertThat(allowance.getRemainingAttempts()).isEqualTo(2);
    }

    @Test
    void restartAttemptCopiesSnapshotOrderAndClearsAnswers() throws Exception {
        QuizAttempt existing = persistedAttempt();
        AttemptQuestion first = existing.getQuestions().get(0);
        first.autosaveAnswer(first.getOptions().get(1).getId(), 4, NOW.minusSeconds(10));
        when(quizAttemptRepository.findByIdAndUserIdWithQuestions(4L, 1L)).thenReturn(Optional.of(existing));

        service.restartAttempt(4L, 1L);

        ArgumentCaptor<QuizAttempt> captor = ArgumentCaptor.forClass(QuizAttempt.class);
        verify(quizAttemptRepository).save(captor.capture());
        QuizAttempt replacement = captor.getValue();
        assertThat(replacement.getQuestions()).extracting(AttemptQuestion::getOriginalQuestionId)
                .containsExactlyElementsOf(existing.getQuestions().stream()
                        .map(AttemptQuestion::getOriginalQuestionId)
                        .toList());
        assertThat(replacement.getQuestions().get(0).getOptions())
                .extracting(option -> option.getOriginalAnswerOptionId())
                .containsExactlyElementsOf(first.getOptions().stream()
                        .map(option -> option.getOriginalAnswerOptionId())
                        .toList());
        assertThat(replacement.getQuestions()).allSatisfy(question -> {
            assertThat(question.getSelectedOptionId()).isNull();
            assertThat(question.getAnswerRevision()).isZero();
            assertThat(question.getAnsweredAt()).isNull();
        });
    }

    @Test
    void restartAttemptWithNoRemainingAttemptsDoesNotAbandonCurrentAttempt() throws Exception {
        QuizAttempt existing = persistedAttempt();
        allowance.consumeRight(NOW.minusSeconds(30));
        allowance.consumeRight(NOW.minusSeconds(20));
        allowance.consumeRight(NOW.minusSeconds(10));
        when(quizAttemptRepository.findByIdAndUserIdWithQuestions(4L, 1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.restartAttempt(4L, 1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("No attempts remaining.");

        assertThat(existing.getStatus()).isEqualTo(AttemptStatus.IN_PROGRESS);
    }

    @Test
    void restartAttemptAutoSubmitsOverdueAttemptAndRejectsRestart() throws Exception {
        QuizAttempt existing = AttemptTestFactory.attempt(4L, user, quiz, NOW.minusSeconds(31 * 60));
        when(quizAttemptRepository.findByIdAndUserIdWithQuestions(4L, 1L)).thenReturn(Optional.of(existing));
        when(scoringService.score(existing)).thenReturn(SCORE);

        assertThatThrownBy(() -> service.restartAttempt(4L, 1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Attempt expired and was automatically submitted.");

        assertThat(existing.getStatus()).isEqualTo(AttemptStatus.COMPLETED);
        assertThat(existing.getCompletionReason()).isEqualTo(AttemptCompletionReason.TIME_EXPIRED);
    }

    @Test
    void autosaveAnswerSavesSelectedOptionAndRevision() throws Exception {
        QuizAttempt attempt = persistedAttempt();
        AttemptQuestion first = attempt.getQuestions().get(0);
        Long selectedOptionId = first.getOptions().get(1).getId();
        when(quizAttemptRepository.findByIdAndUserIdWithQuestions(4L, 1L)).thenReturn(Optional.of(attempt));

        AutosaveAnswerResponse response = service.autosaveAnswer(4L, first.getId(), 1L, selectedOptionId, 1);

        assertThat(response.saved()).isTrue();
        assertThat(first.getSelectedOptionId()).isEqualTo(selectedOptionId);
        assertThat(first.getAnswerRevision()).isEqualTo(1);
    }

    @Test
    void staleAutosaveDoesNotOverwriteNewerAnswer() throws Exception {
        QuizAttempt attempt = persistedAttempt();
        AttemptQuestion first = attempt.getQuestions().get(0);
        Long newerOption = first.getOptions().get(1).getId();
        first.autosaveAnswer(newerOption, 2, NOW.minusSeconds(5));
        when(quizAttemptRepository.findByIdAndUserIdWithQuestions(4L, 1L)).thenReturn(Optional.of(attempt));

        AutosaveAnswerResponse response = service.autosaveAnswer(4L, first.getId(), 1L, first.getOptions().get(0).getId(), 1);

        assertThat(response.stale()).isTrue();
        assertThat(first.getSelectedOptionId()).isEqualTo(newerOption);
        assertThat(first.getAnswerRevision()).isEqualTo(2);
    }

    @Test
    void autosaveRejectsOptionFromAnotherQuestion() throws Exception {
        QuizAttempt attempt = persistedAttempt();
        AttemptQuestion first = attempt.getQuestions().get(0);
        AttemptQuestion second = attempt.getQuestions().get(1);
        when(quizAttemptRepository.findByIdAndUserIdWithQuestions(4L, 1L)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> service.autosaveAnswer(4L, first.getId(), 1L, second.getOptions().get(0).getId(), 1))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Selected option does not belong to this question.");
    }

    @Test
    void autosaveAutoSubmitsWhenOverdue() throws Exception {
        QuizAttempt attempt = AttemptTestFactory.attempt(4L, user, quiz, NOW.minusSeconds(31 * 60));
        AttemptQuestion first = attempt.getQuestions().get(0);
        when(quizAttemptRepository.findByIdAndUserIdWithQuestions(4L, 1L)).thenReturn(Optional.of(attempt));
        when(scoringService.score(attempt)).thenReturn(SCORE);

        AutosaveAnswerResponse response = service.autosaveAnswer(4L, first.getId(), 1L, first.getOptions().get(0).getId(), 1);

        assertThat(response.autoSubmitted()).isTrue();
        assertThat(response.redirectUrl()).isEqualTo("/attempts/4/result");
        assertThat(attempt.getCompletionReason()).isEqualTo(AttemptCompletionReason.TIME_EXPIRED);
    }

    @Test
    void manualSubmitBeforeExpiryCompletesWithManualReason() throws Exception {
        QuizAttempt attempt = persistedAttempt();
        AttemptQuestion first = attempt.getQuestions().get(0);
        AttemptQuestion second = attempt.getQuestions().get(1);
        when(quizAttemptRepository.findByIdAndUserIdWithQuestions(4L, 1L)).thenReturn(Optional.of(attempt));
        when(scoringService.score(attempt)).thenReturn(SCORE);

        service.submitAttempt(4L, 1L, request(
                answer(first, first.getOptions().get(0).getId()),
                answer(second, second.getOptions().get(0).getId())
        ));

        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.COMPLETED);
        assertThat(attempt.getCompletionReason()).isEqualTo(AttemptCompletionReason.MANUAL);
        assertThat(attempt.getSubmittedAt()).isEqualTo(NOW);
        assertThat(allowance.getRemainingAttempts()).isEqualTo(3);
    }

    @Test
    void completionOfLastUsedAttemptStartsCooldown() throws Exception {
        QuizAttempt attempt = persistedAttempt();
        AttemptQuestion first = attempt.getQuestions().get(0);
        AttemptQuestion second = attempt.getQuestions().get(1);
        allowance.consumeRight(NOW.minusSeconds(30));
        allowance.consumeRight(NOW.minusSeconds(20));
        allowance.consumeRight(NOW.minusSeconds(10));
        when(quizAttemptRepository.findByIdAndUserIdWithQuestions(4L, 1L)).thenReturn(Optional.of(attempt));
        when(scoringService.score(attempt)).thenReturn(SCORE);

        service.submitAttempt(4L, 1L, request(
                answer(first, first.getOptions().get(0).getId()),
                answer(second, second.getOptions().get(0).getId())
        ));

        assertThat(allowance.getCooldownUntil()).isEqualTo(NOW.plusSeconds(1440 * 60L));
    }

    @Test
    void cooldownDoesNotStartWhileAnotherFinalAttemptIsActive() throws Exception {
        QuizAttempt attempt = persistedAttempt();
        AttemptQuestion first = attempt.getQuestions().get(0);
        AttemptQuestion second = attempt.getQuestions().get(1);
        allowance.consumeRight(NOW.minusSeconds(30));
        allowance.consumeRight(NOW.minusSeconds(20));
        allowance.consumeRight(NOW.minusSeconds(10));
        when(quizAttemptRepository.findByIdAndUserIdWithQuestions(4L, 1L)).thenReturn(Optional.of(attempt));
        when(quizAttemptRepository.existsByUserIdAndQuizIdAndStatus(1L, 3L, AttemptStatus.IN_PROGRESS))
                .thenReturn(true);
        when(scoringService.score(attempt)).thenReturn(SCORE);

        service.submitAttempt(4L, 1L, request(
                answer(first, first.getOptions().get(0).getId()),
                answer(second, second.getOptions().get(0).getId())
        ));

        assertThat(allowance.getCooldownUntil()).isNull();
    }

    @Test
    void startAttemptBlockedDuringCooldown() {
        allowance.consumeRight(NOW.minusSeconds(30));
        allowance.consumeRight(NOW.minusSeconds(20));
        allowance.consumeRight(NOW.minusSeconds(10));
        allowance.startCooldown(NOW.plusSeconds(3600));
        stubStartDependencies(Optional.empty());

        assertThatThrownBy(() -> service.startAttempt(3L, 1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Quiz is in cooldown.");
    }

    @Test
    void cooldownResetRestoresAttemptLimitBeforeStartConsumesOneRight() {
        allowance.consumeRight(NOW.minusSeconds(30));
        allowance.consumeRight(NOW.minusSeconds(20));
        allowance.consumeRight(NOW.minusSeconds(10));
        allowance.startCooldown(NOW.minusSeconds(1));
        stubStartDependencies(Optional.empty());

        StartQuizResponse response = service.startAttempt(3L, 1L);

        assertThat(response.remainingAttempts()).isEqualTo(2);
        assertThat(response.cooldownUntil()).isNull();
    }

    @Test
    void activeAttemptCanContinueWithNoRemainingRights() throws Exception {
        QuizAttempt existing = AttemptTestFactory.attempt(11L, user, quiz, NOW.minusSeconds(60));
        allowance.consumeRight(NOW.minusSeconds(30));
        allowance.consumeRight(NOW.minusSeconds(20));
        allowance.consumeRight(NOW.minusSeconds(10));
        stubStartDependencies(Optional.of(existing));

        StartQuizResponse response = service.startAttempt(3L, 1L);

        assertThat(response.resumed()).isTrue();
        assertThat(response.remainingAttempts()).isZero();
    }

    @Test
    void freshAttemptSamplesQuestionCountAndUsesRandomizedQuestionAndOptionOrder() throws Exception {
        Category category = AttemptTestFactory.category(12L, "Architecture");
        Question first = AttemptTestFactory.question(101L, "First?", category);
        Question second = AttemptTestFactory.question(102L, "Second?", category);
        Question third = AttemptTestFactory.question(103L, "Third?", category);
        Quiz samplingQuiz = Quiz.create("Sampling Quiz", null, category, 30, 2, 3, 1440, List.of(first, second, third));
        samplingQuiz.publish();
        AttemptTestFactory.setId(samplingQuiz, 13L);
        QuizAttemptAllowance samplingAllowance = QuizAttemptAllowance.initialize(user, samplingQuiz);
        DefaultQuizAttemptCommandService samplingService = new DefaultQuizAttemptCommandService(
                quizAttemptRepository,
                allowanceRepository,
                quizQueryService,
                userQueryService,
                scoringService,
                reversingRandomizer(),
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
        when(userQueryService.getById(1L)).thenReturn(user);
        when(quizQueryService.getPublishedByIdForAttempt(13L)).thenReturn(samplingQuiz);
        when(quizAttemptRepository.findByUserIdAndQuizIdAndStatus(1L, 13L, AttemptStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(allowanceRepository.findByUserIdAndQuizIdForUpdate(1L, 13L))
                .thenReturn(Optional.of(samplingAllowance));

        samplingService.startAttempt(13L, 1L);

        ArgumentCaptor<QuizAttempt> captor = ArgumentCaptor.forClass(QuizAttempt.class);
        verify(quizAttemptRepository).save(captor.capture());
        QuizAttempt saved = captor.getValue();
        assertThat(saved.getQuestions()).hasSize(2);
        assertThat(saved.getQuestions()).extracting(AttemptQuestion::getOriginalQuestionId)
                .containsExactly(102L, 103L);
        assertThat(saved.getQuestions()).extracting(AttemptQuestion::getOriginalQuestionId)
                .doesNotHaveDuplicates();
        assertThat(saved.getQuestions().get(0).getOptions())
                .extracting(option -> option.getOriginalAnswerOptionId())
                .containsExactly(1022L, 1021L);
    }

    @Test
    void manualSubmitRejectsMissingAttemptQuestionEntry() throws Exception {
        QuizAttempt attempt = persistedAttempt();
        AttemptQuestion first = attempt.getQuestions().get(0);
        when(quizAttemptRepository.findByIdAndUserIdWithQuestions(4L, 1L)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> service.submitAttempt(
                4L,
                1L,
                request(answer(first, first.getOptions().get(0).getId()))
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("All attempt questions must be submitted.");
    }

    @Test
    void manualSubmitAllowsPresentQuestionEntryWithNullSelectedOption() throws Exception {
        QuizAttempt attempt = persistedAttempt();
        AttemptQuestion first = attempt.getQuestions().get(0);
        AttemptQuestion second = attempt.getQuestions().get(1);
        when(quizAttemptRepository.findByIdAndUserIdWithQuestions(4L, 1L)).thenReturn(Optional.of(attempt));
        when(scoringService.score(attempt)).thenReturn(new ScoreResult(2, 1, 0, 1, 50, "DEFAULT_V1"));

        service.submitAttempt(4L, 1L, request(
                answer(first, null),
                answer(second, second.getOptions().get(0).getId())
        ));

        assertThat(first.getSelectedOptionId()).isNull();
        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.COMPLETED);
        assertThat(attempt.getCompletionReason()).isEqualTo(AttemptCompletionReason.MANUAL);
    }

    @Test
    void manualSubmitRejectsDuplicateQuestionEntry() throws Exception {
        QuizAttempt attempt = persistedAttempt();
        AttemptQuestion first = attempt.getQuestions().get(0);
        AttemptQuestion second = attempt.getQuestions().get(1);
        when(quizAttemptRepository.findByIdAndUserIdWithQuestions(4L, 1L)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> service.submitAttempt(4L, 1L, request(
                answer(first, first.getOptions().get(0).getId()),
                answer(first, first.getOptions().get(1).getId()),
                answer(second, second.getOptions().get(0).getId())
        )))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Duplicate answers are not allowed.");
    }

    @Test
    void manualSubmitRejectsUnknownQuestionEntry() throws Exception {
        QuizAttempt attempt = persistedAttempt();
        AttemptQuestion first = attempt.getQuestions().get(0);
        when(quizAttemptRepository.findByIdAndUserIdWithQuestions(4L, 1L)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> service.submitAttempt(4L, 1L, request(
                answer(first, first.getOptions().get(0).getId()),
                answer(999L, null)
        )))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Attempt question not found.");
    }

    @Test
    void manualSubmitRejectsSelectedOptionFromAnotherQuestion() throws Exception {
        QuizAttempt attempt = persistedAttempt();
        AttemptQuestion first = attempt.getQuestions().get(0);
        AttemptQuestion second = attempt.getQuestions().get(1);
        when(quizAttemptRepository.findByIdAndUserIdWithQuestions(4L, 1L)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> service.submitAttempt(4L, 1L, request(
                answer(first, second.getOptions().get(0).getId()),
                answer(second, second.getOptions().get(1).getId())
        )))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Selected option does not belong to this question.");
    }

    @Test
    void manualSubmitAfterExpiryIgnoresIncompletePayloadAndCompletesAsTimeExpired() throws Exception {
        QuizAttempt attempt = AttemptTestFactory.attempt(4L, user, quiz, NOW.minusSeconds(31 * 60));
        when(quizAttemptRepository.findByIdAndUserIdWithQuestions(4L, 1L)).thenReturn(Optional.of(attempt));
        when(scoringService.score(attempt)).thenReturn(SCORE);

        service.submitAttempt(4L, 1L, request());

        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.COMPLETED);
        assertThat(attempt.getCompletionReason()).isEqualTo(AttemptCompletionReason.TIME_EXPIRED);
        assertThat(attempt.getSubmittedAt()).isEqualTo(attempt.getExpiresAt());
    }

    @Test
    void autoSubmitOverdueAttemptsForUserAffectsOnlyRepositoryReturnedAttempts() throws Exception {
        QuizAttempt overdue = AttemptTestFactory.attempt(4L, user, quiz, NOW.minusSeconds(31 * 60));
        when(quizAttemptRepository.findByUserIdAndStatusAndExpiresAtLessThanEqual(1L, AttemptStatus.IN_PROGRESS, NOW))
                .thenReturn(List.of(overdue));
        when(scoringService.score(overdue)).thenReturn(SCORE);

        int count = service.autoSubmitOverdueAttemptsForUser(1L);

        assertThat(count).isEqualTo(1);
        assertThat(overdue.getCompletionReason()).isEqualTo(AttemptCompletionReason.TIME_EXPIRED);
    }

    private void stubStartDependencies(Optional<QuizAttempt> existing) {
        when(userQueryService.getById(1L)).thenReturn(user);
        when(quizQueryService.getPublishedByIdForAttempt(3L)).thenReturn(quiz);
        when(quizAttemptRepository.findByUserIdAndQuizIdAndStatus(1L, 3L, AttemptStatus.IN_PROGRESS))
                .thenReturn(existing);
    }

    private QuizAttempt persistedAttempt() throws Exception {
        return AttemptTestFactory.attempt(4L, user, quiz, NOW.minusSeconds(60));
    }

    private AttemptRandomizer reversingRandomizer() {
        return new AttemptRandomizer() {
            @Override
            public <T> List<T> shuffledCopy(List<T> source) {
                List<T> copy = new ArrayList<>(source);
                Collections.reverse(copy);
                return copy;
            }
        };
    }

    private SubmitQuizRequest request(UserAnswerRequest... answers) {
        SubmitQuizRequest request = new SubmitQuizRequest();
        request.setAnswers(List.of(answers));
        return request;
    }

    private UserAnswerRequest answer(AttemptQuestion question, Long selectedOptionId) {
        return answer(question.getId(), selectedOptionId);
    }

    private UserAnswerRequest answer(Long attemptQuestionId, Long selectedOptionId) {
        UserAnswerRequest answer = new UserAnswerRequest();
        answer.setAttemptQuestionId(attemptQuestionId);
        answer.setSelectedOptionId(selectedOptionId);
        return answer;
    }
}
