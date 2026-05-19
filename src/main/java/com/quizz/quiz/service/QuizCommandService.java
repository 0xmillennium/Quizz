package com.quizz.quiz.service;

import com.quizz.quiz.dto.QuizCreateRequest;
import com.quizz.quiz.dto.QuizUpdateRequest;
import com.quizz.quiz.entity.Quiz;

/**
 * Write boundary for quiz definitions.
 *
 * <p>
 * Commands manage draft creation, draft updates, publishing, and archiving.
 * They validate cross-aggregate authoring rules such as active categories,
 * active questions, pool size, and answer-option correctness before a quiz is
 * made available for attempts.
 * </p>
 */
public interface QuizCommandService {

    /**
     * Creates a draft quiz definition with an explicit question pool.
     */
    Quiz create(QuizCreateRequest request);

    /**
     * Updates only a draft quiz.
     *
     * <p>
     * Published quizzes are immutable through this contract except for the
     * archive lifecycle transition.
     * </p>
     */
    Quiz updateDraft(Long quizId, QuizUpdateRequest request);

    /**
     * Publishes a draft quiz after validating active category, active questions,
     * option correctness, and {@code questionCount <= pool size}.
     */
    void publish(Long quizId);

    /**
     * Archives a quiz without changing existing attempt snapshots.
     */
    void archive(Long quizId);
}
