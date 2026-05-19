package com.quizz.quiz.service;

import com.quizz.quiz.entity.Quiz;
import java.util.List;

/**
 * Read boundary for quiz definitions.
 *
 * <p>Query methods provide admin views, public published views, and fetch graphs
 * needed to start attempts. Implementations must not mutate quiz lifecycle
 * state or bypass publication checks for public/user-facing reads.</p>
 */
public interface QuizQueryService {

    Quiz getById(Long quizId);

    Quiz getByIdWithAdminDetails(Long quizId);

    Quiz getPublishedById(Long quizId);

    Quiz getPublishedByIdForAttempt(Long quizId);

    List<Quiz> findAllForAdmin();

    List<Quiz> findPublished();

    List<Quiz> findPublishedByCategory(Long categoryId);
}
