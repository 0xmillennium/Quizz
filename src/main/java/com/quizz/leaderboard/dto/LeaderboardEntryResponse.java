package com.quizz.leaderboard.dto;

import java.time.Instant;

/**
 * Public leaderboard ranking row.
 *
 * <p>
 * The row exposes display identity and ranking metrics only. User email is
 * intentionally absent from the public leaderboard model.
 * </p>
 */
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
        Instant submittedAt) {
}
