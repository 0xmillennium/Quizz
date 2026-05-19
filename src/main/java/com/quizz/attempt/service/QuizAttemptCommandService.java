package com.quizz.attempt.service;

import com.quizz.attempt.dto.AutoSubmitResponse;
import com.quizz.attempt.dto.AutosaveAnswerResponse;
import com.quizz.attempt.dto.StartQuizResponse;
import com.quizz.attempt.dto.SubmitQuizRequest;
import com.quizz.attempt.dto.SubmitQuizResponse;

/**
 * Command boundary for attempt lifecycle mutations.
 *
 * <p>
 * This service owns start, resume, restart, autosave, manual submit,
 * time-expired submit, allowance updates, and cooldown transitions. Callers
 * pass the authenticated user id so implementations can resolve attempts by
 * both attempt id and user id rather than trusting client-provided ownership.
 * </p>
 */
public interface QuizAttemptCommandService {

    /**
     * Starts or resumes a quiz attempt for a user.
     *
     * <p>
     * An active non-overdue attempt is resumed without consuming another
     * right. An overdue active attempt is auto-submitted first. A fresh attempt
     * consumes one right, samples a randomized immutable snapshot, and is blocked
     * when the allowance is in cooldown.
     * </p>
     */
    StartQuizResponse startAttempt(Long quizId, Long userId);

    /**
     * Restarts an active in-progress attempt using the same snapshot.
     *
     * <p>
     * The method requires an active attempt and an available remaining right.
     * It does not draw a new random pool; the replacement copies the previous
     * sampled questions and options to avoid question-pool fishing. If the
     * existing attempt is overdue, it is auto-submitted instead of restarted.
     * </p>
     */
    StartQuizResponse restartAttempt(Long attemptId, Long userId);

    /**
     * Saves one answer for an active attempt question.
     *
     * <p>
     * The attempt is resolved by attempt id and user id, the selected option
     * must belong to the attempt question, and {@code answerRevision} prevents a
     * stale browser request from overwriting newer saved state. Autosave does
     * not score an active attempt; an overdue autosave completes it with
     * {@code TIME_EXPIRED} from the already-saved answers.
     * </p>
     */
    AutosaveAnswerResponse autosaveAnswer(
            Long attemptId,
            Long attemptQuestionId,
            Long userId,
            Long selectedOptionId,
            int answerRevision);

    /**
     * Auto-submits an overdue attempt if it is still in progress.
     *
     * <p>
     * Scoring uses the saved attempt snapshot, {@code submittedAt} is the
     * attempt expiry time, and allowance/cooldown state is updated after the
     * terminal transition. Already completed attempts return their current
     * terminal response.
     * </p>
     */
    AutoSubmitResponse autoSubmitIfOverdue(Long attemptId, Long userId);

    /**
     * Reconciles all stale overdue attempts for one user-facing boundary.
     *
     * <p>
     * The query is scoped to the supplied user id and must not mutate other
     * users' attempts.
     * </p>
     */
    int autoSubmitOverdueAttemptsForUser(Long userId);

    /**
     * Submits an attempt manually or resolves it as time-expired.
     *
     * <p>
     * Before expiry, the submitted payload must contain one answer entry for
     * every attempt question and those answers are saved before scoring with
     * {@code MANUAL}. After expiry, the payload is ignored and scoring uses the
     * previously saved answers with {@code TIME_EXPIRED}.
     * </p>
     */
    SubmitQuizResponse submitAttempt(
            Long attemptId,
            Long userId,
            SubmitQuizRequest request);
}
