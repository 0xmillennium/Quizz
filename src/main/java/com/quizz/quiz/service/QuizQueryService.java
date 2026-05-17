package com.quizz.quiz.service;

import com.quizz.quiz.entity.Quiz;
import java.util.List;

public interface QuizQueryService {

    Quiz getById(Long quizId);

    Quiz getByIdWithAdminDetails(Long quizId);

    Quiz getPublishedById(Long quizId);

    Quiz getPublishedByIdForAttempt(Long quizId);

    List<Quiz> findAllForAdmin();

    List<Quiz> findPublished();

    List<Quiz> findPublishedByCategory(Long categoryId);
}
