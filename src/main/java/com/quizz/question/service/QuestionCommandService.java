package com.quizz.question.service;

import com.quizz.question.dto.QuestionCreateRequest;
import com.quizz.question.dto.QuestionUpdateRequest;
import com.quizz.question.entity.Question;

/**
 * Write boundary for the question-bank aggregate.
 *
 * <p>Commands create and update questions with owned answer options and enforce
 * authoring invariants such as option count and exactly one correct option.
 * Archive and restore are lifecycle transitions; callers should not hard-delete
 * questions that may be referenced by quizzes or attempt snapshots.</p>
 */
public interface QuestionCommandService {

    /**
     * Creates a question and its owned answer options as one aggregate change.
     */
    Question create(QuestionCreateRequest request);

    /**
     * Replaces editable question text, category, and owned options.
     *
     * <p>The service validates that the replacement option set still has
     * exactly one correct option.</p>
     */
    Question update(Long questionId, QuestionUpdateRequest request);

    /**
     * Archives a question instead of deleting it.
     */
    void archive(Long questionId);

    /**
     * Restores an archived question to active authoring use.
     */
    void restore(Long questionId);
}
