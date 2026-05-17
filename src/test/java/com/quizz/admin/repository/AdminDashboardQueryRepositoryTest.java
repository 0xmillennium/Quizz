package com.quizz.admin.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quizz.admin.repository.AdminDashboardQueryRepository.DashboardMetricsRow;
import com.quizz.admin.repository.AdminDashboardQueryRepository.RecentAttemptRow;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@ExtendWith(MockitoExtension.class)
class AdminDashboardQueryRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private ResultSet resultSet;

    private AdminDashboardQueryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new AdminDashboardQueryRepository(jdbcTemplate);
    }

    @Test
    void fetchMetricsQueriesExpectedTables() {
        DashboardMetricsRow metrics = new DashboardMetricsRow(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1.0);
        when(jdbcTemplate.queryForObject(anyString(), anyMap(), any(RowMapper.class))).thenReturn(metrics);

        repository.fetchMetrics();

        String sql = captureMetricsQuery().sql();
        assertThat(sql).contains("FROM users");
        assertThat(sql).contains("FROM categories");
        assertThat(sql).contains("FROM questions");
        assertThat(sql).contains("FROM quizzes");
        assertThat(sql).contains("FROM quiz_attempts");
        assertThat(sql).contains("status = 'ACTIVE'");
        assertThat(sql).contains("status = 'DRAFT'");
        assertThat(sql).contains("status = 'PUBLISHED'");
        assertThat(sql).contains("status = 'ARCHIVED'");
        assertThat(sql).contains("status = 'IN_PROGRESS'");
        assertThat(sql).contains("status = 'COMPLETED'");
        assertThat(sql).contains("status = 'EXPIRED'");
        assertThat(sql).contains("COALESCE(AVG(score_percentage), 0)");
        assertThat(sql).doesNotContain("email");
    }

    @Test
    void findRecentAttemptsPassesLimitOrdersByStartedAtDescAndDoesNotSelectEmail() {
        when(jdbcTemplate.query(anyString(), anyMap(), any(RowMapper.class))).thenReturn(List.of());

        repository.findRecentAttempts(10);

        CapturedQuery<RecentAttemptRow> query = captureRecentQuery();
        assertThat(query.params().get("limit")).isEqualTo(10);
        assertThat(query.sql()).contains("ORDER BY a.started_at DESC");
        assertThat(query.sql()).contains("LIMIT :limit");
        assertThat(query.sql()).contains("JOIN users u ON u.id = a.user_id");
        assertThat(query.sql()).doesNotContain("email");
    }

    @Test
    void recentRowMapperMapsFields() throws Exception {
        Instant startedAt = Instant.parse("2026-01-01T10:00:00Z");
        Instant submittedAt = Instant.parse("2026-01-01T10:15:00Z");
        when(jdbcTemplate.query(anyString(), anyMap(), any(RowMapper.class))).thenReturn(List.of());
        when(resultSet.getLong("attempt_id")).thenReturn(1L);
        when(resultSet.getLong("user_id")).thenReturn(2L);
        when(resultSet.getString("user_full_name")).thenReturn("Ada Lovelace");
        when(resultSet.getString("quiz_title")).thenReturn("Science Quiz");
        when(resultSet.getString("category_name")).thenReturn("Science");
        when(resultSet.getString("status")).thenReturn("COMPLETED");
        when(resultSet.getInt("score_percentage")).thenReturn(80);
        when(resultSet.getTimestamp("started_at")).thenReturn(Timestamp.from(startedAt));
        when(resultSet.getTimestamp("submitted_at")).thenReturn(Timestamp.from(submittedAt));

        repository.findRecentAttempts(10);
        RecentAttemptRow row = captureRecentQuery().rowMapper().mapRow(resultSet, 0);

        assertThat(row.attemptId()).isEqualTo(1L);
        assertThat(row.userId()).isEqualTo(2L);
        assertThat(row.userFullName()).isEqualTo("Ada Lovelace");
        assertThat(row.quizTitle()).isEqualTo("Science Quiz");
        assertThat(row.categoryName()).isEqualTo("Science");
        assertThat(row.status()).isEqualTo("COMPLETED");
        assertThat(row.scorePercentage()).isEqualTo(80);
        assertThat(row.startedAt()).isEqualTo(startedAt);
        assertThat(row.submittedAt()).isEqualTo(submittedAt);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private CapturedQuery<DashboardMetricsRow> captureMetricsQuery() {
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, ?>> paramsCaptor = ArgumentCaptor.forClass((Class) Map.class);
        ArgumentCaptor<RowMapper<DashboardMetricsRow>> rowMapperCaptor = ArgumentCaptor.forClass((Class) RowMapper.class);
        verify(jdbcTemplate).queryForObject(sqlCaptor.capture(), paramsCaptor.capture(), rowMapperCaptor.capture());
        return new CapturedQuery<>(sqlCaptor.getValue(), paramsCaptor.getValue(), rowMapperCaptor.getValue());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private CapturedQuery<RecentAttemptRow> captureRecentQuery() {
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, ?>> paramsCaptor = ArgumentCaptor.forClass((Class) Map.class);
        ArgumentCaptor<RowMapper<RecentAttemptRow>> rowMapperCaptor = ArgumentCaptor.forClass((Class) RowMapper.class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), paramsCaptor.capture(), rowMapperCaptor.capture());
        return new CapturedQuery<>(sqlCaptor.getValue(), paramsCaptor.getValue(), rowMapperCaptor.getValue());
    }

    private record CapturedQuery<T>(
            String sql,
            Map<String, ?> params,
            RowMapper<T> rowMapper
    ) {
    }
}
