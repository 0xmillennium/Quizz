package com.quizz.quiz.service;

/**
 * Read boundary for the per-user attempt state shown on a quiz detail page.
 *
 * <p>
 * The provider derives active attempt, latest result, remaining rights,
 * cooldown, and stale auto-submit notices without exposing repository details
 * to the MVC controller.
 * </p>
 */
public interface QuizAttemptStateProvider {

    QuizAttemptStateResponse resolveForQuizDetail(Long quizId, Long userId);
}
