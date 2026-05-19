/**
 * Quiz command, query, and user attempt-state contracts.
 *
 * <p>Quiz services preserve the separation between authored quiz definitions
 * and per-user attempt lifecycle state. Published quiz reads are kept behind
 * service methods so controllers do not depend on repository fetch graphs.</p>
 */
package com.quizz.quiz.service;
