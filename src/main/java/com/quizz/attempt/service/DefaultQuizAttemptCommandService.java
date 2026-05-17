package com.quizz.attempt.service;

import com.quizz.attempt.dto.QuizResultResponse;
import com.quizz.attempt.dto.StartQuizResponse;
import com.quizz.attempt.dto.SubmitQuizRequest;
import com.quizz.attempt.dto.UserAnswerRequest;
import com.quizz.attempt.entity.AttemptAnswerOption;
import com.quizz.attempt.entity.AttemptQuestion;
import com.quizz.attempt.entity.AttemptStatus;
import com.quizz.attempt.entity.QuizAttempt;
import com.quizz.attempt.mapper.QuizAttemptMapper;
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
    private final QuizAttemptMapper quizAttemptMapper;
    private final Clock clock;

    public DefaultQuizAttemptCommandService(
            QuizAttemptRepository quizAttemptRepository,
            QuizQueryService quizQueryService,
            UserQueryService userQueryService,
            ScoringService scoringService,
            QuizAttemptMapper quizAttemptMapper,
            Clock clock
    ) {
        this.quizAttemptRepository = quizAttemptRepository;
        this.quizQueryService = quizQueryService;
        this.userQueryService = userQueryService;
        this.scoringService = scoringService;
        this.quizAttemptMapper = quizAttemptMapper;
        this.clock = clock;
    }

    @Override
    public StartQuizResponse startAttempt(Long quizId, Long userId) {
        Instant now = Instant.now(clock);
        User user = userQueryService.getById(userId);
        Quiz quiz = quizQueryService.getPublishedByIdForAttempt(quizId);

        quizAttemptRepository.findByUserIdAndQuizIdAndStatus(userId, quizId, AttemptStatus.IN_PROGRESS)
                .ifPresent(existing -> handleExistingInProgressAttempt(existing, now));

        QuizAttempt attempt = QuizAttempt.start(user, quiz, now);
        QuizAttempt saved = quizAttemptRepository.save(attempt);
        return new StartQuizResponse(saved.getId());
    }

    @Override
    public QuizResultResponse submitAttempt(
            Long attemptId,
            Long userId,
            SubmitQuizRequest request
    ) {
        Instant now = Instant.now(clock);
        QuizAttempt attempt = quizAttemptRepository.findByIdAndUserIdWithQuestions(attemptId, userId)
                .orElseThrow(() -> new NotFoundException("Attempt not found."));

        if (!attempt.isInProgress()) {
            throw new BusinessRuleException("Attempt is not in progress.");
        }

        if (attempt.isExpiredAt(now)) {
            attempt.markExpired();
            loadQuestionOptions(attempt);
            return quizAttemptMapper.toResultResponse(attempt);
        }

        loadQuestionOptions(attempt);
        Map<Long, Long> submittedAnswers = validatedAnswers(request, attempt);
        for (AttemptQuestion question : attempt.getQuestions()) {
            attempt.answerQuestion(question.getId(), submittedAnswers.get(question.getId()));
        }

        ScoreResult scoreResult = scoringService.score(attempt);
        attempt.complete(now, scoreResult);
        return quizAttemptMapper.toResultResponse(attempt);
    }

    @Override
    public void expireOverdueAttempts() {
        Instant now = Instant.now(clock);
        quizAttemptRepository.findByStatusAndExpiresAtBeforeOrAt(AttemptStatus.IN_PROGRESS, now)
                .forEach(QuizAttempt::markExpired);
    }

    private void handleExistingInProgressAttempt(QuizAttempt existing, Instant now) {
        if (!existing.isExpiredAt(now)) {
            throw new BusinessRuleException("You already have an active attempt for this quiz.");
        }
        existing.markExpired();
        quizAttemptRepository.flush();
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
            validateSelectedOptionBelongsToQuestion(attempt, questionId, answer.getSelectedOptionId());
            submittedAnswers.put(questionId, answer.getSelectedOptionId());
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

    private void validateSelectedOptionBelongsToQuestion(
            QuizAttempt attempt,
            Long questionId,
            Long selectedOptionId
    ) {
        if (selectedOptionId == null) {
            return;
        }
        AttemptQuestion question = attempt.getQuestions().stream()
                .filter(candidate -> questionId.equals(candidate.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("Attempt question not found."));
        boolean optionBelongsToQuestion = question.getOptions().stream()
                .map(AttemptAnswerOption::getId)
                .anyMatch(selectedOptionId::equals);
        if (!optionBelongsToQuestion) {
            throw new BusinessRuleException("Selected option does not belong to this question.");
        }
    }
}
