package com.quizz.attempt.service;

import com.quizz.attempt.dto.ResultChartResponse;
import com.quizz.attempt.entity.AttemptQuestion;
import com.quizz.attempt.entity.QuizAttempt;
import com.quizz.attempt.mapper.QuizAttemptMapper;
import com.quizz.attempt.repository.QuizAttemptRepository;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.common.exception.NotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DefaultQuizAttemptQueryService implements QuizAttemptQueryService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAttemptMapper quizAttemptMapper;
    private final Clock clock;

    public DefaultQuizAttemptQueryService(
            QuizAttemptRepository quizAttemptRepository,
            QuizAttemptMapper quizAttemptMapper,
            Clock clock) {
        this.quizAttemptRepository = quizAttemptRepository;
        this.quizAttemptMapper = quizAttemptMapper;
        this.clock = clock;
    }

    @Override
    public QuizAttempt getAttemptPage(Long attemptId, Long userId) {
        QuizAttempt attempt = quizAttemptRepository.findByIdAndUserIdWithQuestions(attemptId, userId)
                .orElseThrow(() -> new NotFoundException("Attempt not found."));
        if (!attempt.isInProgress()) {
            throw new BusinessRuleException("Attempt is not in progress.");
        }
        if (attempt.isOverdueAt(Instant.now(clock))) {
            throw new BusinessRuleException("Attempt has expired.");
        }
        loadQuestionOptions(attempt);
        return attempt;
    }

    @Override
    public QuizAttempt getResult(Long attemptId, Long userId) {
        QuizAttempt attempt = quizAttemptRepository.findResultByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new NotFoundException("Attempt not found."));
        if (!attempt.isCompleted()) {
            throw new BusinessRuleException("Only completed attempts have results.");
        }
        loadQuestionOptions(attempt);
        return attempt;
    }

    @Override
    public List<QuizAttempt> findHistoryByUser(Long userId) {
        return quizAttemptRepository.findHistoryByUserId(userId);
    }

    @Override
    public ResultChartResponse getResultChart(Long attemptId, Long userId) {
        return quizAttemptMapper.toChartResponse(getResult(attemptId, userId));
    }

    private void loadQuestionOptions(QuizAttempt attempt) {
        List<Long> questionIds = attempt.getQuestions().stream()
                .map(AttemptQuestion::getId)
                .toList();
        if (!questionIds.isEmpty()) {
            quizAttemptRepository.findQuestionsWithOptionsByIdIn(questionIds);
        }
    }
}
