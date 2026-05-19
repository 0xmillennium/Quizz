package com.quizz.admin.service;

import com.quizz.admin.dto.AdminDashboardResponse;
import com.quizz.admin.dto.AdminRecentAttemptResponse;
import com.quizz.admin.repository.AdminDashboardQueryRepository;
import com.quizz.admin.repository.AdminDashboardQueryRepository.DashboardMetricsRow;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DefaultAdminDashboardService implements AdminDashboardService {

    private static final int RECENT_ATTEMPT_LIMIT = 10;

    private final AdminDashboardQueryRepository repository;

    public DefaultAdminDashboardService(AdminDashboardQueryRepository repository) {
        this.repository = repository;
    }

    @Override
    public AdminDashboardResponse getDashboard() {
        DashboardMetricsRow metrics = repository.fetchMetrics();
        List<AdminRecentAttemptResponse> recentAttempts = repository.findRecentAttempts(RECENT_ATTEMPT_LIMIT).stream()
                .map(row -> new AdminRecentAttemptResponse(
                        row.attemptId(),
                        row.userId(),
                        row.userFullName(),
                        row.quizTitle(),
                        row.categoryName(),
                        row.status(),
                        row.completionReason(),
                        row.scorePercentage(),
                        row.startedAt(),
                        row.submittedAt(),
                        row.abandonedAt()))
                .toList();

        return new AdminDashboardResponse(
                metrics.totalUsers(),
                metrics.enabledUsers(),
                metrics.totalCategories(),
                metrics.activeCategories(),
                metrics.totalQuestions(),
                metrics.activeQuestions(),
                metrics.totalQuizzes(),
                metrics.draftQuizzes(),
                metrics.publishedQuizzes(),
                metrics.archivedQuizzes(),
                metrics.totalAttempts(),
                metrics.inProgressAttempts(),
                metrics.completedAttempts(),
                metrics.abandonedAttempts(),
                metrics.manualCompletedAttempts(),
                metrics.timeExpiredCompletedAttempts(),
                metrics.averageScorePercentage(),
                recentAttempts);
    }
}
