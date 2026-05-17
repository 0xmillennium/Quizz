package com.quizz.quiz.service;

import com.quizz.common.exception.NotFoundException;
import com.quizz.quiz.entity.Quiz;
import com.quizz.quiz.entity.QuizStatus;
import com.quizz.quiz.repository.QuizRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DefaultQuizQueryService implements QuizQueryService {

    private final QuizRepository quizRepository;

    public DefaultQuizQueryService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    @Override
    public Quiz getById(Long quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz not found."));
    }

    @Override
    public Quiz getByIdWithAdminDetails(Long quizId) {
        return quizRepository.findByIdWithAdminDetails(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz not found."));
    }

    @Override
    public Quiz getPublishedById(Long quizId) {
        return quizRepository.findByIdAndStatusWithDetails(quizId, QuizStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Published quiz not found."));
    }

    @Override
    public Quiz getPublishedByIdForAttempt(Long quizId) {
        return quizRepository.findByIdAndStatusWithAttemptGraph(quizId, QuizStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Published quiz not found."));
    }

    @Override
    public List<Quiz> findAllForAdmin() {
        return quizRepository.findAllWithCategoryAndQuestionsForAdmin();
    }

    @Override
    public List<Quiz> findPublished() {
        return quizRepository.findByStatusWithCategoryAndQuestions(QuizStatus.PUBLISHED);
    }

    @Override
    public List<Quiz> findPublishedByCategory(Long categoryId) {
        return quizRepository.findByCategoryIdAndStatusWithCategoryAndQuestions(categoryId, QuizStatus.PUBLISHED);
    }
}
