package com.quizz.quiz.service;

import com.quizz.quiz.dto.QuizCreateRequest;
import com.quizz.quiz.dto.QuizUpdateRequest;
import com.quizz.quiz.entity.Quiz;

public interface QuizCommandService {

    Quiz create(QuizCreateRequest request);

    Quiz updateDraft(Long quizId, QuizUpdateRequest request);

    void publish(Long quizId);

    void archive(Long quizId);
}
