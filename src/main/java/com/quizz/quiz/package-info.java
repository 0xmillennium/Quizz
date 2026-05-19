/**
 * Quiz definitions and published question pools.
 *
 * <p>
 * A quiz defines policy such as duration, sampled question count, attempt
 * limit, and retake cooldown. {@code QuizQuestion} represents pool membership,
 * not an attempt-time fixed order, and quizzes avoid {@code ManyToMany}
 * coupling so publication and attempt snapshots can enforce their own
 * boundaries.
 * </p>
 */
package com.quizz.quiz;
