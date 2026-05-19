package com.quizz.leaderboard.service;

import com.quizz.leaderboard.dto.LeaderboardFilterRequest;
import com.quizz.leaderboard.dto.LeaderboardViewResponse;

/**
 * Read-only service for public leaderboard views.
 *
 * <p>
 * The service delegates deterministic ranking to the query repository and
 * returns only completed submitted attempts. It must not expose user email
 * addresses or create leaderboard persistence state.
 * </p>
 */
public interface LeaderboardService {

    LeaderboardViewResponse getLeaderboard(LeaderboardFilterRequest filter);
}
