package com.quizz.attempt.entity;

public record AutosaveOutcome(
        boolean saved,
        boolean stale) {
    public static AutosaveOutcome savedOutcome() {
        return new AutosaveOutcome(true, false);
    }

    public static AutosaveOutcome staleOutcome() {
        return new AutosaveOutcome(false, true);
    }
}
