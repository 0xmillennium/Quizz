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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DefaultQuizAttemptCommandService implements QuizAttemptCommandService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizQueryService quizQueryService;
    private final UserQueryService userQueryService;
    private final ScoringService scoringService;
    private final Clock clock;

    public DefaultQuizAttemptCommandService(
            QuizAttemptRepository quizAttemptRepository,
            QuizQueryService quizQueryService,
            UserQueryService userQueryService,
            ScoringService scoringService,
            Clock clock
    ) {
        this.quizAttemptRepository = quizAttemptRepository;
        this.quizQueryService = quizQueryService;
        this.userQueryService = userQueryService;
        this.scoringService = scoringService;
        this.clock = clock;
    }

    @Override
    public StartQuizResponse startAttempt(Long quizId, Long userId) {
        Instant now = Instant.now(clock);
        User user = userQueryService.getById(userId);
        Quiz quiz = quizQueryService.getPublishedByIdForAttempt(quizId);

        QuizAttempt existing = quizAttemptRepository.findByUserIdAndQuizIdAndStatus(
                userId,
                quizId,
                AttemptStatus.IN_PROGRESS
        ).orElse(null);
        if (existing != null && !existing.isOverdueAt(now)) {
            return new StartQuizResponse(existing.getId(), true, false, null);
        }
        if (existing != null) {
            autoSubmitExisting(existing);
            quizAttemptRepository.flush();
        }

        QuizAttempt attempt = QuizAttempt.start(user, quiz, now);
        QuizAttempt saved = quizAttemptRepository.save(attempt);
        return new StartQuizResponse(
                saved.getId(),
                false,
                existing != null,
                existing == null ? null : existing.getId()
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

        boolean autoSubmitted = false;
        if (existing.isOverdueAt(now)) {
            autoSubmitExisting(existing);
            autoSubmitted = true;
        } else {
            existing.abandonForRestart(now);
        }
        quizAttemptRepository.flush();

        QuizAttempt replacement = QuizAttempt.start(existing.getUser(), existing.getQuiz(), now);
        QuizAttempt saved = quizAttemptRepository.save(replacement);
        return new StartQuizResponse(saved.getId(), false, autoSubmitted, existing.getId());
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
            autoSubmitExisting(attempt);
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
        overdueAttempts.forEach(this::autoSubmitExisting);
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
        return new SubmitQuizResponse(attempt.getId(), attempt.getCompletionReason().name());
    }

    private void autoSubmitExisting(QuizAttempt attempt) {
        if (!attempt.isInProgress()) {
            return;
        }
        loadQuestionOptions(attempt);
        ScoreResult scoreResult = scoringService.score(attempt);
        attempt.completeByTimeExpiry(scoreResult);
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
