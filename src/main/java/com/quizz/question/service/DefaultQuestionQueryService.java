package com.quizz.question.service;

import com.quizz.common.exception.BusinessRuleException;
import com.quizz.common.exception.NotFoundException;
import com.quizz.question.entity.Question;
import com.quizz.question.entity.QuestionStatus;
import com.quizz.question.repository.QuestionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DefaultQuestionQueryService implements QuestionQueryService {

    private final QuestionRepository questionRepository;

    public DefaultQuestionQueryService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public Question getById(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found."));
    }

    @Override
    public Question getByIdWithDetails(Long questionId) {
        return questionRepository.findByIdWithDetails(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found."));
    }

    @Override
    public Question getActiveById(Long questionId) {
        Question question = getById(questionId);
        if (!question.isActive()) {
            throw new BusinessRuleException("Question is archived.");
        }
        return question;
    }

    @Override
    public List<Question> findAllForAdmin() {
        return questionRepository.findAllWithDetailsForAdmin();
    }

    @Override
    public List<Question> findActive() {
        return questionRepository.findByStatusWithCategory(QuestionStatus.ACTIVE);
    }

    @Override
    public List<Question> findActiveByCategory(Long categoryId) {
        return questionRepository.findByCategoryIdAndStatusWithCategory(categoryId, QuestionStatus.ACTIVE);
    }

    @Override
    public long countActiveByCategory(Long categoryId) {
        return questionRepository.countByCategoryIdAndStatus(categoryId, QuestionStatus.ACTIVE);
    }
}
