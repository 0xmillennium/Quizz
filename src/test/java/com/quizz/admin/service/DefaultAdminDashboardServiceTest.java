package com.quizz.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quizz.admin.dto.AdminDashboardResponse;
import com.quizz.admin.repository.AdminDashboardQueryRepository;
import com.quizz.admin.repository.AdminDashboardQueryRepository.DashboardMetricsRow;
import com.quizz.admin.repository.AdminDashboardQueryRepository.RecentAttemptRow;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultAdminDashboardServiceTest {

    @Mock
    private AdminDashboardQueryRepository repository;

    private DefaultAdminDashboardService service;

    @BeforeEach
    void setUp() {
        service = new DefaultAdminDashboardService(repository);
    }

    @Test
    void getDashboardFetchesMetricsAndRecentAttemptsWithLimitTen() {
        DashboardMetricsRow metrics = metrics();
        Instant startedAt = Instant.parse("2026-01-01T10:00:00Z");
        Instant submittedAt = Instant.parse("2026-01-01T10:15:00Z");
        RecentAttemptRow recentAttempt = new RecentAttemptRow(
                1L,
                2L,
                "Ada Lovelace",
                "Science Quiz",
                "Science",
                "COMPLETED",
                80,
                startedAt,
                submittedAt
        );
        when(repository.fetchMetrics()).thenReturn(metrics);
        when(repository.findRecentAttempts(10)).thenReturn(List.of(recentAttempt));

        AdminDashboardResponse response = service.getDashboard();

        verify(repository).fetchMetrics();
        verify(repository).findRecentAttempts(10);
        assertThat(response.totalUsers()).isEqualTo(10);
        assertThat(response.enabledUsers()).isEqualTo(9);
        assertThat(response.totalCategories()).isEqualTo(8);
        assertThat(response.activeCategories()).isEqualTo(7);
        assertThat(response.totalQuestions()).isEqualTo(6);
        assertThat(response.activeQuestions()).isEqualTo(5);
        assertThat(response.totalQuizzes()).isEqualTo(4);
        assertThat(response.draftQuizzes()).isEqualTo(1);
        assertThat(response.publishedQuizzes()).isEqualTo(2);
        assertThat(response.archivedQuizzes()).isEqualTo(1);
        assertThat(response.totalAttempts()).isEqualTo(30);
        assertThat(response.inProgressAttempts()).isEqualTo(3);
        assertThat(response.completedAttempts()).isEqualTo(20);
        assertThat(response.expiredAttempts()).isEqualTo(7);
        assertThat(response.averageScorePercentage()).isEqualTo(76.5);
        assertThat(response.recentAttempts()).singleElement().satisfies(attempt -> {
            assertThat(attempt.attemptId()).isEqualTo(1L);
            assertThat(attempt.userId()).isEqualTo(2L);
            assertThat(attempt.userFullName()).isEqualTo("Ada Lovelace");
            assertThat(attempt.quizTitle()).isEqualTo("Science Quiz");
            assertThat(attempt.categoryName()).isEqualTo("Science");
            assertThat(attempt.status()).isEqualTo("COMPLETED");
            assertThat(attempt.scorePercentage()).isEqualTo(80);
            assertThat(attempt.startedAt()).isEqualTo(startedAt);
            assertThat(attempt.submittedAt()).isEqualTo(submittedAt);
        });
    }

    @Test
    void getDashboardAllowsEmptyRecentAttempts() {
        when(repository.fetchMetrics()).thenReturn(metrics());
        when(repository.findRecentAttempts(10)).thenReturn(List.of());

        AdminDashboardResponse response = service.getDashboard();

        assertThat(response.recentAttempts()).isEmpty();
        assertThat(response.averageScorePercentage()).isEqualTo(76.5);
    }

    private DashboardMetricsRow metrics() {
        return new DashboardMetricsRow(
                10,
                9,
                8,
                7,
                6,
                5,
                4,
                1,
                2,
                1,
                30,
                3,
                20,
                7,
                76.5
        );
    }
}
