package com.quizz.quiz.service;

public interface QuizAttemptStateProvider {

    QuizAttemptStateResponse resolveForQuizDetail(Long quizId, Long userId);
}
