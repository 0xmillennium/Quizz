package com.quizz.admin.dto;

import java.util.List;

public record AdminDashboardResponse(
        long totalUsers,
        long enabledUsers,
        long totalCategories,
        long activeCategories,
        long totalQuestions,
        long activeQuestions,
        long totalQuizzes,
        long draftQuizzes,
        long publishedQuizzes,
        long archivedQuizzes,
        long totalAttempts,
        long inProgressAttempts,
        long completedAttempts,
        long abandonedAttempts,
        long manualCompletedAttempts,
        long timeExpiredCompletedAttempts,
        double averageScorePercentage,
        List<AdminRecentAttemptResponse> recentAttempts) {
}
