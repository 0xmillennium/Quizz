/**
 * User quiz attempt lifecycle, snapshots, scoring, and attempt rights.
 *
 * <p>The attempt package owns start, resume, restart, autosave, manual submit,
 * time-expired submit, scoring, and cooldown behavior. Attempt snapshots are
 * immutable records of the sampled quiz content, so user-facing play models
 * must not expose correctness while scoring, result, and admin reporting may
 * use snapshot correctness.</p>
 */
package com.quizz.attempt;
