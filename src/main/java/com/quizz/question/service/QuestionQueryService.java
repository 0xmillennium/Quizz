package com.quizz.question.service;

import com.quizz.question.entity.Question;
import java.util.List;

/**
 * Read boundary for question-bank data.
 *
 * <p>
 * Query methods shape admin lists, authoring selectors, and active question
 * counts. They must not mutate question lifecycle or expose correctness to
 * active attempt play DTOs; the attempt package owns that DTO boundary.
 * </p>
 */
public interface QuestionQueryService {

    Question getById(Long questionId);

    Question getByIdWithDetails(Long questionId);

    Question getActiveById(Long questionId);

    List<Question> findAllForAdmin();

    List<Question> findActive();

    List<Question> findActiveByCategory(Long categoryId);

    long countActiveByCategory(Long categoryId);
}
