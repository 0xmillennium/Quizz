package com.quizz.attempt.scoring;

public record ScoreResult(
        int totalQuestions,
        int correctCount,
        int wrongCount,
        int unansweredCount,
        int scorePercentage,
        String scoringVersion) {
}
