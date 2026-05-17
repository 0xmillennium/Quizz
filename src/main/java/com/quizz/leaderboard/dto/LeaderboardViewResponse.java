package com.quizz.leaderboard.dto;

import java.util.List;

public record LeaderboardViewResponse(
        String scope,
        Long quizId,
        Long categoryId,
        int limit,
        List<LeaderboardEntryResponse> entries
) {
}
