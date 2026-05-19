package com.quizz.attempt.service;

import com.quizz.attempt.dto.AutoSubmitResponse;
import com.quizz.attempt.dto.AutosaveAnswerResponse;
import com.quizz.attempt.dto.StartQuizResponse;
import com.quizz.attempt.dto.SubmitQuizRequest;
import com.quizz.attempt.dto.SubmitQuizResponse;
import com.quizz.attempt.dto.UserAnswerRequest;
import com.quizz.attempt.entity.AttemptAnswerOption;
import com.quizz.attempt.entity.AttemptQuestion;
import com.quizz.attempt.entity.AttemptStatus;
import com.quizz.attempt.entity.AutosaveOutcome;
import com.quizz.attempt.entity.QuizAttempt;
import com.quizz.attempt.entity.QuizAttemptAllowance;
import com.quizz.attempt.random.AttemptRandomizer;
import com.quizz.attempt.repository.QuizAttemptAllowanceRepository;
import com.quizz.attempt.repository.QuizAttemptRepository;
import com.quizz.attempt.scoring.ScoreResult;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.common.exception.NotFoundException;
import com.quizz.quiz.entity.Quiz;
import com.quizz.quiz.service.QuizQueryService;
import com.quizz.user.entity.User;
import com.quizz.user.service.UserQueryService;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactional implementation of attempt lifecycle commands.
 *
 * <p>The implementation coordinates quiz/user reads, attempt snapshots,
 * persisted answer state, scoring, and allowance locking in one write boundary.
 * Controllers should depend on {@link QuizAttemptCommandService}, not on the
 * attempt repositories directly.</p>
 */
@Service
@Transactional
public class DefaultQuizAttemptCommandService implements QuizAttemptCommandService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAttemptAllowanceRepository allowanceRepository;
    private final QuizQueryService quizQueryService;
    private final UserQueryService userQueryService;
    private final ScoringService scoringService;
    private final AttemptRandomizer attemptRandomizer;
    private final Clock clock;

    public DefaultQuizAttemptCommandService(
            QuizAttemptRepository quizAttemptRepository,
            QuizAttemptAllowanceRepository allowanceRepository,
            QuizQueryService quizQueryService,
            UserQueryService userQueryService,
            ScoringService scoringService,
            AttemptRandomizer attemptRandomizer,
            Clock clock
    ) {
        this.quizAttemptRepository = quizAttemptRepository;
        this.allowanceRepository = allowanceRepository;
        this.quizQueryService = quizQueryService;
        this.userQueryService = userQueryService;
        this.scoringService = scoringService;
        this.attemptRandomizer = attemptRandomizer;
        this.clock = clock;
    }

    @Override
    public StartQuizResponse startAttempt(Long quizId, Long userId) {
        Instant now = Instant.now(clock);
        User user = userQueryService.getById(userId);
        Quiz quiz = quizQueryService.getPublishedByIdForAttempt(quizId);
        QuizAttemptAllowance allowance = getOrCreateAllowanceForUpdate(user, quiz);

        QuizAttempt existing = quizAttemptRepository.findByUserIdAndQuizIdAndStatus(
                userId,
                quizId,
                AttemptStatus.IN_PROGRESS
        ).orElse(null);
        if (existing != null && !existing.isOverdueAt(now)) {
            allowance.resetIfCooldownExpired(quiz, now);
            return new StartQuizResponse(
                    existing.getId(),
                    true,
                    false,
                    null,
                    allowance.getRemainingAttempts(),
                    allowance.getCooldownUntil()
            );
        }
        boolean previousAttemptAutoSubmitted = false;
        if (existing != null) {
            autoSubmitExisting(existing);
            quizAttemptRepository.flush();
            updateAllowanceAfterTerminal(existing, allowance);
            previousAttemptAutoSubmitted = true;
        }

        allowance.resetIfCooldownExpired(quiz, now);
        ensureCanStart(allowance);
        allowance.consumeRight(now);

        QuizAttempt attempt = createFreshAttempt(user, quiz, now);
        QuizAttempt saved = quizAttemptRepository.save(attempt);
        return new StartQuizResponse(
                saved.getId(),
                false,
                previousAttemptAutoSubmitted,
                existing == null ? null : existing.getId(),
                allowance.getRemainingAttempts(),
                allowance.getCooldownUntil()
        );
    }

    @Override
    public StartQuizResponse restartAttempt(Long attemptId, Long userId) {
        Instant now = Instant.now(clock);
        QuizAttempt existing = quizAttemptRepository.findByIdAndUserIdWithQuestions(attemptId, userId)
                .orElseThrow(() -> new NotFoundException("Attempt not found."));
        if (!existing.isInProgress()) {
            throw new BusinessRuleException("Only an active attempt can be restarted.");
        }

        if (existing.isOverdueAt(now)) {
            autoSubmitExisting(existing);
            QuizAttemptAllowance allowance = getOrCreateAllowanceForUpdate(existing.getUser(), existing.getQuiz());
            updateAllowanceAfterTerminal(existing, allowance);
            quizAttemptRepository.flush();
            throw new BusinessRuleException("Attempt expired and was automatically submitted.");
        } else {
            QuizAttemptAllowance allowance = getOrCreateAllowanceForUpdate(existing.getUser(), existing.getQuiz());
            allowance.resetIfCooldownExpired(existing.getQuiz(), now);
            ensureCanStart(allowance);
            if (allowance.getRemainingAttempts() <= 0) {
                throw new BusinessRuleException("No restart attempts remaining.");
            }
            loadQuestionOptions(existing);
            // Restart reuses the previous snapshot so users cannot repeatedly
            // restart to sample the entire question pool.
            existing.abandonForRestart(now);
            quizAttemptRepository.flush();
            allowance.consumeRight(now);
            QuizAttempt replacement = QuizAttempt.restartFromSnapshot(existing, now);
            QuizAttempt saved = quizAttemptRepository.save(replacement);
            return new StartQuizResponse(
                    saved.getId(),
                    false,
                    false,
                    existing.getId(),
                    allowance.getRemainingAttempts(),
                    allowance.getCooldownUntil()
            );
        }
    }

    @Override
    public AutosaveAnswerResponse autosaveAnswer(
            Long attemptId,
            Long attemptQuestionId,
            Long userId,
            Long selectedOptionId,
            int answerRevision
    ) {
        if (answerRevision < 1) {
            throw new BusinessRuleException("Answer revision must be positive.");
        }

        Instant now = Instant.now(clock);
        QuizAttempt attempt = quizAttemptRepository.findByIdAndUserIdWithQuestions(attemptId, userId)
                .orElseThrow(() -> new NotFoundException("Attempt not found."));
        if (attempt.isCompleted()) {
            throw new BusinessRuleException("Completed attempts cannot be changed.");
        }
        if (attempt.isAbandoned()) {
            throw new BusinessRuleException("Abandoned attempts cannot be changed.");
        }
        if (attempt.isOverdueAt(now)) {
            // Overdue autosave uses previously persisted answers; the incoming
            // browser payload is no longer part of the logical quiz window.
            autoSubmitExisting(attempt);
            updateAllowanceAfterTerminal(attempt);
            return new AutosaveAnswerResponse(
                    attempt.getId(),
                    attemptQuestionId,
                    selectedOptionId,
                    answerRevision,
                    attempt.getStatus().name(),
                    false,
                    false,
                    true,
                    resultUrl(attempt.getId()),
                    null,
                    attempt.getExpiresAt()
            );
        }

        loadQuestionOptions(attempt);
        AttemptQuestion question = findAttemptQuestion(attempt, attemptQuestionId);
        validateSelectedOptionBelongsToQuestion(question, selectedOptionId);
        AutosaveOutcome outcome = question.autosaveAnswer(selectedOptionId, answerRevision, now);
        return new AutosaveAnswerResponse(
                attempt.getId(),
                question.getId(),
                question.getSelectedOptionId(),
                question.getAnswerRevision(),
                outcome.saved() ? "SAVED" : "STALE",
                outcome.saved(),
                outcome.stale(),
                false,
                null,
                outcome.saved() ? question.getAnsweredAt() : null,
                attempt.getExpiresAt()
        );
    }

    @Override
    public AutoSubmitResponse autoSubmitIfOverdue(Long attemptId, Long userId) {
        Instant now = Instant.now(clock);
        QuizAttempt attempt = quizAttemptRepository.findByIdAndUserIdWithQuestions(attemptId, userId)
                .orElseThrow(() -> new NotFoundException("Attempt not found."));
        if (attempt.isCompleted()) {
            return autoSubmitResponse(attempt);
        }
        if (attempt.isAbandoned()) {
            throw new BusinessRuleException("Abandoned attempts cannot be submitted.");
        }
        if (!attempt.isOverdueAt(now)) {
            throw new BusinessRuleException("Attempt has not expired yet.");
        }
        autoSubmitExisting(attempt);
        updateAllowanceAfterTerminal(attempt);
        return autoSubmitResponse(attempt);
    }

    @Override
    public int autoSubmitOverdueAttemptsForUser(Long userId) {
        Instant now = Instant.now(clock);
        List<QuizAttempt> overdueAttempts = quizAttemptRepository.findByUserIdAndStatusAndExpiresAtLessThanEqual(
                userId,
                AttemptStatus.IN_PROGRESS,
                now
        );
        overdueAttempts.forEach(attempt -> {
            autoSubmitExisting(attempt);
            updateAllowanceAfterTerminal(attempt);
        });
        return overdueAttempts.size();
    }

    @Override
    public SubmitQuizResponse submitAttempt(
            Long attemptId,
            Long userId,
            SubmitQuizRequest request
    ) {
        Instant now = Instant.now(clock);
        QuizAttempt attempt = quizAttemptRepository.findByIdAndUserIdWithQuestions(attemptId, userId)
                .orElseThrow(() -> new NotFoundException("Attempt not found."));
        if (attempt.isCompleted()) {
            throw new BusinessRuleException("Attempt is already completed.");
        }
        if (attempt.isAbandoned()) {
            throw new BusinessRuleException("Abandoned attempts cannot be submitted.");
        }

        loadQuestionOptions(attempt);
        if (attempt.isOverdueAt(now)) {
            ScoreResult scoreResult = scoringService.score(attempt);
            attempt.completeByTimeExpiry(scoreResult);
            updateAllowanceAfterTerminal(attempt);
            return new SubmitQuizResponse(attempt.getId(), attempt.getCompletionReason().name());
        }

        Map<Long, Long> submittedAnswers = validatedAnswers(request, attempt);
        for (AttemptQuestion question : attempt.getQuestions()) {
            question.autosaveAnswer(
                    submittedAnswers.get(question.getId()),
                    question.getAnswerRevision() + 1,
                    now
            );
        }

        ScoreResult scoreResult = scoringService.score(attempt);
        attempt.completeManually(now, scoreResult);
        updateAllowanceAfterTerminal(attempt);
        return new SubmitQuizResponse(attempt.getId(), attempt.getCompletionReason().name());
    }

    private QuizAttempt createFreshAttempt(User user, Quiz quiz, Instant now) {
        List<com.quizz.quiz.entity.QuizQuestion> shuffledPool = attemptRandomizer.shuffledCopy(quiz.getQuestions());
        if (shuffledPool.size() < quiz.getQuestionCount()) {
            throw new BusinessRuleException("Quiz question pool is smaller than questions per attempt.");
        }
        List<com.quizz.quiz.entity.QuizQuestion> sampledQuestions = shuffledPool
                .subList(0, quiz.getQuestionCount());
        List<com.quizz.quiz.entity.QuizQuestion> questionOrder = attemptRandomizer.shuffledCopy(sampledQuestions);
        return QuizAttempt.start(
                user,
                quiz,
                now,
                questionOrder,
                question -> attemptRandomizer.shuffledCopy(question.getOptions())
        );
    }

    private void autoSubmitExisting(QuizAttempt attempt) {
        if (!attempt.isInProgress()) {
            return;
        }
        loadQuestionOptions(attempt);
        ScoreResult scoreResult = scoringService.score(attempt);
        attempt.completeByTimeExpiry(scoreResult);
    }

    private QuizAttemptAllowance getOrCreateAllowanceForUpdate(User user, Quiz quiz) {
        return allowanceRepository.findByUserIdAndQuizIdForUpdate(user.getId(), quiz.getId())
                .orElseGet(() -> allowanceRepository.save(QuizAttemptAllowance.initialize(user, quiz)));
    }

    private void ensureCanStart(QuizAttemptAllowance allowance) {
        if (allowance.getCooldownUntil() != null) {
            throw new BusinessRuleException("Quiz is in cooldown.");
        }
        if (allowance.getRemainingAttempts() <= 0) {
            throw new BusinessRuleException("No attempts remaining.");
        }
    }

    private void updateAllowanceAfterTerminal(QuizAttempt attempt) {
        QuizAttemptAllowance allowance = getOrCreateAllowanceForUpdate(attempt.getUser(), attempt.getQuiz());
        updateAllowanceAfterTerminal(attempt, allowance);
    }

    private void updateAllowanceAfterTerminal(QuizAttempt attempt, QuizAttemptAllowance allowance) {
        if (allowance.getRemainingAttempts() != 0 || allowance.getCooldownUntil() != null) {
            return;
        }
        boolean hasActiveAttempt = quizAttemptRepository.existsByUserIdAndQuizIdAndStatus(
                attempt.getUser().getId(),
                attempt.getQuiz().getId(),
                AttemptStatus.IN_PROGRESS
        );
        // Cooldown starts only when exhausted rights are no longer paired with
        // an active attempt the user can finish or restart.
        if (hasActiveAttempt) {
            return;
        }
        Instant terminalAt = attempt.isCompleted() ? attempt.getSubmittedAt() : attempt.getAbandonedAt();
        if (terminalAt == null) {
            return;
        }
        allowance.startCooldown(terminalAt.plus(attempt.getQuiz().getRetakeCooldownMinutes(), ChronoUnit.MINUTES));
    }

    private AutoSubmitResponse autoSubmitResponse(QuizAttempt attempt) {
        return new AutoSubmitResponse(
                attempt.getId(),
                attempt.getStatus().name(),
                attempt.getCompletionReason() == null ? null : attempt.getCompletionReason().name(),
                resultUrl(attempt.getId())
        );
    }

    private Map<Long, Long> validatedAnswers(SubmitQuizRequest request, QuizAttempt attempt) {
        List<UserAnswerRequest> answers = request == null || request.getAnswers() == null
                ? List.of()
                : request.getAnswers();
        Set<Long> knownQuestionIds = attempt.getQuestions().stream()
                .map(AttemptQuestion::getId)
                .collect(Collectors.toSet());

        Map<Long, Long> submittedAnswers = new HashMap<>();
        for (UserAnswerRequest answer : answers) {
            if (answer == null || answer.getAttemptQuestionId() == null) {
                throw new BusinessRuleException("Attempt question is required.");
            }
            Long questionId = answer.getAttemptQuestionId();
            if (!knownQuestionIds.contains(questionId)) {
                throw new BusinessRuleException("Attempt question not found.");
            }
            if (submittedAnswers.containsKey(questionId)) {
                throw new BusinessRuleException("Duplicate answers are not allowed.");
            }
            submittedAnswers.put(questionId, answer.getSelectedOptionId());
        }
        if (!submittedAnswers.keySet().equals(knownQuestionIds)) {
            throw new BusinessRuleException("All attempt questions must be submitted.");
        }
        for (Map.Entry<Long, Long> submittedAnswer : submittedAnswers.entrySet()) {
            AttemptQuestion question = findAttemptQuestion(attempt, submittedAnswer.getKey());
            validateSelectedOptionBelongsToQuestion(question, submittedAnswer.getValue());
        }
        return submittedAnswers;
    }

    private void loadQuestionOptions(QuizAttempt attempt) {
        List<Long> questionIds = attempt.getQuestions().stream()
                .map(AttemptQuestion::getId)
                .toList();
        if (!questionIds.isEmpty()) {
            quizAttemptRepository.findQuestionsWithOptionsByIdIn(questionIds);
        }
    }

    private AttemptQuestion findAttemptQuestion(QuizAttempt attempt, Long attemptQuestionId) {
        return attempt.getQuestions().stream()
                .filter(question -> Objects.equals(question.getId(), attemptQuestionId))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("Attempt question not found."));
    }

    private void validateSelectedOptionBelongsToQuestion(AttemptQuestion question, Long selectedOptionId) {
        if (selectedOptionId == null) {
            return;
        }
        boolean optionBelongsToQuestion = question.getOptions().stream()
                .map(AttemptAnswerOption::getId)
                .anyMatch(selectedOptionId::equals);
        if (!optionBelongsToQuestion) {
            throw new BusinessRuleException("Selected option does not belong to this question.");
        }
    }

    private String resultUrl(Long attemptId) {
        return "/attempts/" + attemptId + "/result";
    }
}
