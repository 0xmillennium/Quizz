package com.quizz.leaderboard.dto;

import java.time.Instant;

public record LeaderboardEntryResponse(
        int rankPosition,
        Long userId,
        String userFullName,
        Long quizId,
        String quizTitle,
        Long categoryId,
        String categoryName,
        int totalQuestions,
        int correctCount,
        int scorePercentage,
        Instant submittedAt
) {
}
