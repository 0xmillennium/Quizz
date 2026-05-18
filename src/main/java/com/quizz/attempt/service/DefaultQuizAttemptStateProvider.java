package com.quizz.attempt.service;

import com.quizz.attempt.entity.AttemptStatus;
import com.quizz.attempt.entity.QuizAttempt;
import com.quizz.attempt.repository.QuizAttemptRepository;
import com.quizz.quiz.service.QuizAttemptStateProvider;
import com.quizz.quiz.service.QuizAttemptStateResponse;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DefaultQuizAttemptStateProvider implements QuizAttemptStateProvider {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAttemptCommandService quizAttemptCommandService;
    private final Clock clock;

    public DefaultQuizAttemptStateProvider(
            QuizAttemptRepository quizAttemptRepository,
            QuizAttemptCommandService quizAttemptCommandService,
            Clock clock
    ) {
        this.quizAttemptRepository = quizAttemptRepository;
        this.quizAttemptCommandService = quizAttemptCommandService;
        this.clock = clock;
    }

    @Override
    public QuizAttemptStateResponse resolveForQuizDetail(Long quizId, Long userId) {
        return quizAttemptRepository.findByUserIdAndQuizIdAndStatus(userId, quizId, AttemptStatus.IN_PROGRESS)
                .map(attempt -> resolveActiveAttempt(attempt, userId))
                .orElse(new QuizAttemptStateResponse(null, null, null, null));
    }

    private QuizAttemptStateResponse resolveActiveAttempt(QuizAttempt attempt, Long userId) {
        if (attempt.isOverdueAt(Instant.now(clock))) {
            quizAttemptCommandService.autoSubmitIfOverdue(attempt.getId(), userId);
            return new QuizAttemptStateResponse(null, null, null, attempt.getId());
        }
        return new QuizAttemptStateResponse(
                attempt.getId(),
                attempt.getStartedAt(),
                attempt.getExpiresAt(),
                null
        );
    }
}
