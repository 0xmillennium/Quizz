package com.quizz.leaderboard.dto;

import java.util.List;

/**
 * Public leaderboard view model for one ranking scope.
 *
 * <p>The entries are already ordered by the query model's deterministic ranking
 * rules and contain no user email addresses.</p>
 */
public record LeaderboardViewResponse(
        String scope,
        Long quizId,
        Long categoryId,
        int limit,
        List<LeaderboardEntryResponse> entries
) {
}
