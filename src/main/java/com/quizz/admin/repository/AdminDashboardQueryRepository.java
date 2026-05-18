package com.quizz.admin.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AdminDashboardQueryRepository {

    private static final String METRICS_SQL = """
            SELECT
                (SELECT COUNT(*) FROM users) AS total_users,
                (SELECT COUNT(*) FROM users WHERE enabled = true) AS enabled_users,
                (SELECT COUNT(*) FROM categories) AS total_categories,
                (SELECT COUNT(*) FROM categories WHERE active = true) AS active_categories,
                (SELECT COUNT(*) FROM questions) AS total_questions,
                (SELECT COUNT(*) FROM questions WHERE status = 'ACTIVE') AS active_questions,
                (SELECT COUNT(*) FROM quizzes) AS total_quizzes,
                (SELECT COUNT(*) FROM quizzes WHERE status = 'DRAFT') AS draft_quizzes,
                (SELECT COUNT(*) FROM quizzes WHERE status = 'PUBLISHED') AS published_quizzes,
                (SELECT COUNT(*) FROM quizzes WHERE status = 'ARCHIVED') AS archived_quizzes,
                (SELECT COUNT(*) FROM quiz_attempts) AS total_attempts,
                (SELECT COUNT(*) FROM quiz_attempts WHERE status = 'IN_PROGRESS') AS in_progress_attempts,
                (SELECT COUNT(*) FROM quiz_attempts WHERE status = 'COMPLETED') AS completed_attempts,
                (SELECT COUNT(*) FROM quiz_attempts WHERE status = 'ABANDONED') AS abandoned_attempts,
                (SELECT COUNT(*) FROM quiz_attempts
                 WHERE status = 'COMPLETED' AND completion_reason = 'MANUAL') AS manual_completed_attempts,
                (SELECT COUNT(*) FROM quiz_attempts
                 WHERE status = 'COMPLETED' AND completion_reason = 'TIME_EXPIRED') AS time_expired_completed_attempts,
                (SELECT COALESCE(AVG(score_percentage), 0)
                 FROM quiz_attempts
                 WHERE status = 'COMPLETED') AS average_score_percentage
            """;

    private static final String RECENT_ATTEMPTS_SQL = """
            SELECT
                a.id AS attempt_id,
                u.id AS user_id,
                u.full_name AS user_full_name,
                a.quiz_title_snapshot AS quiz_title,
                a.category_name_snapshot AS category_name,
                a.status,
                a.completion_reason,
                a.score_percentage,
                a.started_at,
                a.submitted_at,
                a.abandoned_at
            FROM quiz_attempts a
            JOIN users u ON u.id = a.user_id
            ORDER BY a.started_at DESC
            LIMIT :limit
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AdminDashboardQueryRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DashboardMetricsRow fetchMetrics() {
        return jdbcTemplate.queryForObject(METRICS_SQL, Map.of(), this::mapMetricsRow);
    }

    public List<RecentAttemptRow> findRecentAttempts(int limit) {
        return jdbcTemplate.query(RECENT_ATTEMPTS_SQL, Map.of("limit", limit), this::mapRecentAttemptRow);
    }

    private DashboardMetricsRow mapMetricsRow(ResultSet rs, int rowNum) throws SQLException {
        return new DashboardMetricsRow(
                rs.getLong("total_users"),
                rs.getLong("enabled_users"),
                rs.getLong("total_categories"),
                rs.getLong("active_categories"),
                rs.getLong("total_questions"),
                rs.getLong("active_questions"),
                rs.getLong("total_quizzes"),
                rs.getLong("draft_quizzes"),
                rs.getLong("published_quizzes"),
                rs.getLong("archived_quizzes"),
                rs.getLong("total_attempts"),
                rs.getLong("in_progress_attempts"),
                rs.getLong("completed_attempts"),
                rs.getLong("abandoned_attempts"),
                rs.getLong("manual_completed_attempts"),
                rs.getLong("time_expired_completed_attempts"),
                rs.getDouble("average_score_percentage")
        );
    }

    private RecentAttemptRow mapRecentAttemptRow(ResultSet rs, int rowNum) throws SQLException {
        return new RecentAttemptRow(
                rs.getLong("attempt_id"),
                rs.getLong("user_id"),
                rs.getString("user_full_name"),
                rs.getString("quiz_title"),
                rs.getString("category_name"),
                rs.getString("status"),
                rs.getString("completion_reason"),
                rs.getInt("score_percentage"),
                toInstant(rs.getTimestamp("started_at")),
                toInstant(rs.getTimestamp("submitted_at")),
                toInstant(rs.getTimestamp("abandoned_at"))
        );
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    public record DashboardMetricsRow(
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
            double averageScorePercentage
    ) {
    }

    public record RecentAttemptRow(
            Long attemptId,
            Long userId,
            String userFullName,
            String quizTitle,
            String categoryName,
            String status,
            String completionReason,
            int scorePercentage,
            Instant startedAt,
            Instant submittedAt,
            Instant abandonedAt
    ) {
    }
}
