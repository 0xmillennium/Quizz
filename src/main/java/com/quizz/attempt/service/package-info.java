/**
 * Attempt command and query service boundaries.
 *
 * <p>
 * Command services mutate attempt, snapshot, scoring, and allowance state.
 * Query services prepare play, result, and history reads and must not change
 * attempt lifecycle state except through explicitly named command
 * collaborators.
 * </p>
 */
package com.quizz.attempt.service;
