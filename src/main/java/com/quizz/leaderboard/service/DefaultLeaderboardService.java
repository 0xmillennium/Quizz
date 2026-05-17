package com.quizz.leaderboard.service;

import com.quizz.category.service.CategoryQueryService;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.leaderboard.dto.LeaderboardEntryResponse;
import com.quizz.leaderboard.dto.LeaderboardFilterRequest;
import com.quizz.leaderboard.dto.LeaderboardViewResponse;
import com.quizz.leaderboard.repository.LeaderboardQueryRepository;
import com.quizz.leaderboard.repository.LeaderboardQueryRepository.LeaderboardRow;
import com.quizz.quiz.service.QuizQueryService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DefaultLeaderboardService implements LeaderboardService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;

    private final LeaderboardQueryRepository leaderboardQueryRepository;
    private final QuizQueryService quizQueryService;
    private final CategoryQueryService categoryQueryService;

    public DefaultLeaderboardService(
            LeaderboardQueryRepository leaderboardQueryRepository,
            QuizQueryService quizQueryService,
            CategoryQueryService categoryQueryService
    ) {
        this.leaderboardQueryRepository = leaderboardQueryRepository;
        this.quizQueryService = quizQueryService;
        this.categoryQueryService = categoryQueryService;
    }

    @Override
    public LeaderboardViewResponse getLeaderboard(LeaderboardFilterRequest filter) {
        LeaderboardFilterRequest safeFilter = filter == null ? new LeaderboardFilterRequest() : filter;
        Long quizId = safeFilter.getQuizId();
        Long categoryId = safeFilter.getCategoryId();
        int limit = normalizeLimit(safeFilter.getLimit());

        if (quizId != null && categoryId != null) {
            throw new BusinessRuleException("Select either quiz or category filter, not both.");
        }

        List<LeaderboardRow> rows;
        String scope;
        if (quizId != null) {
            quizQueryService.getById(quizId);
            rows = leaderboardQueryRepository.findTopByQuiz(quizId, limit);
            scope = "QUIZ";
        } else if (categoryId != null) {
            categoryQueryService.getById(categoryId);
            rows = leaderboardQueryRepository.findTopByCategory(categoryId, limit);
            scope = "CATEGORY";
        } else {
            rows = leaderboardQueryRepository.findTopOverall(limit);
            scope = "OVERALL";
        }

        return new LeaderboardViewResponse(
                scope,
                quizId,
                categoryId,
                limit,
                rows.stream().map(this::toEntryResponse).toList()
        );
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private LeaderboardEntryResponse toEntryResponse(LeaderboardRow row) {
        return new LeaderboardEntryResponse(
                row.rankPosition(),
                row.userId(),
                row.userFullName(),
                row.quizId(),
                row.quizTitle(),
                row.categoryId(),
                row.categoryName(),
                row.totalQuestions(),
                row.correctCount(),
                row.scorePercentage(),
                row.submittedAt()
        );
    }
}
