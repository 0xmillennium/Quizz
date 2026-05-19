/**
 * Question-bank authoring and lifecycle.
 *
 * <p>{@code Question} owns its answer options and controls archive/restore
 * state. Answer-option correctness is a domain property used for scoring and
 * reporting, but play-page DTOs must be shaped by the attempt package so that
 * correctness is not leaked to quiz takers.</p>
 */
package com.quizz.question;
