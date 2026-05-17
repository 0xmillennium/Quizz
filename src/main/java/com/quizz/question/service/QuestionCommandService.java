package com.quizz.question.service;

import com.quizz.question.dto.QuestionCreateRequest;
import com.quizz.question.dto.QuestionUpdateRequest;
import com.quizz.question.entity.Question;

public interface QuestionCommandService {

    Question create(QuestionCreateRequest request);

    Question update(Long questionId, QuestionUpdateRequest request);

    void archive(Long questionId);

    void restore(Long questionId);
}
