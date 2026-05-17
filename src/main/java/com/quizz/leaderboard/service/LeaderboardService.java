package com.quizz.leaderboard.service;

import com.quizz.leaderboard.dto.LeaderboardFilterRequest;
import com.quizz.leaderboard.dto.LeaderboardViewResponse;

public interface LeaderboardService {

    LeaderboardViewResponse getLeaderboard(LeaderboardFilterRequest filter);
}
