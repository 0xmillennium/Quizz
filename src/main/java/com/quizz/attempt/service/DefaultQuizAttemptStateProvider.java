package com.quizz.attempt.service;

import com.quizz.attempt.entity.AttemptStatus;
import com.quizz.attempt.entity.QuizAttempt;
import com.quizz.attempt.entity.QuizAttemptAllowance;
import com.quizz.attempt.repository.QuizAttemptAllowanceRepository;
import com.quizz.attempt.repository.QuizAttemptRepository;
import com.quizz.quiz.entity.Quiz;
import com.quizz.quiz.service.QuizAttemptStateProvider;
import com.quizz.quiz.service.QuizAttemptStateResponse;
import com.quizz.quiz.service.QuizQueryService;
import com.quizz.user.entity.User;
import com.quizz.user.service.UserQueryService;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DefaultQuizAttemptStateProvider implements QuizAttemptStateProvider {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAttemptAllowanceRepository allowanceRepository;
    private final QuizAttemptCommandService quizAttemptCommandService;
    private final QuizQueryService quizQueryService;
    private final UserQueryService userQueryService;
    private final Clock clock;

    public DefaultQuizAttemptStateProvider(
            QuizAttemptRepository quizAttemptRepository,
            QuizAttemptAllowanceRepository allowanceRepository,
            QuizAttemptCommandService quizAttemptCommandService,
            QuizQueryService quizQueryService,
            UserQueryService userQueryService,
            Clock clock) {
        this.quizAttemptRepository = quizAttemptRepository;
        this.allowanceRepository = allowanceRepository;
        this.quizAttemptCommandService = quizAttemptCommandService;
        this.quizQueryService = quizQueryService;
        this.userQueryService = userQueryService;
        this.clock = clock;
    }

    @Override
    public QuizAttemptStateResponse resolveForQuizDetail(Long quizId, Long userId) {
        Instant now = Instant.now(clock);
        Quiz quiz = quizQueryService.getPublishedById(quizId);
        User user = userQueryService.getById(userId);
        QuizAttempt activeAttempt = quizAttemptRepository.findByUserIdAndQuizIdAndStatus(
                userId,
                quizId,
                AttemptStatus.IN_PROGRESS).orElse(null);

        Long autoSubmittedAttemptId = null;
        if (activeAttempt != null && activeAttempt.isOverdueAt(now)) {
            quizAttemptCommandService.autoSubmitIfOverdue(activeAttempt.getId(), userId);
            autoSubmittedAttemptId = activeAttempt.getId();
            activeAttempt = null;
        }

        QuizAttemptAllowance allowance = allowanceRepository.findByUserIdAndQuizIdForUpdate(userId, quizId)
                .orElseGet(() -> allowanceRepository.save(QuizAttemptAllowance.initialize(user, quiz)));
        allowance.resetIfCooldownExpired(quiz, now);

        Long latestCompletedAttemptId = quizAttemptRepository
                .findFirstByUserIdAndQuizIdAndStatusOrderBySubmittedAtDescStartedAtDesc(
                        userId,
                        quizId,
                        AttemptStatus.COMPLETED)
                .map(QuizAttempt::getId)
                .orElse(null);

        boolean canContinue = activeAttempt != null;
        boolean canStart = activeAttempt == null
                && allowance.getCooldownUntil() == null
                && allowance.getRemainingAttempts() > 0;
        boolean canRestart = activeAttempt != null && allowance.getRemainingAttempts() > 0;

        return new QuizAttemptStateResponse(
                activeAttempt == null ? null : activeAttempt.getId(),
                activeAttempt == null ? null : activeAttempt.getStartedAt(),
                activeAttempt == null ? null : activeAttempt.getExpiresAt(),
                latestCompletedAttemptId,
                autoSubmittedAttemptId,
                quiz.getAttemptLimit(),
                allowance.getRemainingAttempts(),
                allowance.getCooldownUntil(),
                canStart,
                canContinue,
                canRestart);
    }
}
