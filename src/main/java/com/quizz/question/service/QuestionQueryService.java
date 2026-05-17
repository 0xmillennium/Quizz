package com.quizz.question.service;

import com.quizz.question.entity.Question;
import java.util.List;

public interface QuestionQueryService {

    Question getById(Long questionId);

    Question getByIdWithDetails(Long questionId);

    Question getActiveById(Long questionId);

    List<Question> findAllForAdmin();

    List<Question> findActive();

    List<Question> findActiveByCategory(Long categoryId);

    long countActiveByCategory(Long categoryId);
}
