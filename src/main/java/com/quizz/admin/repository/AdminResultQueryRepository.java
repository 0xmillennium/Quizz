package com.quizz.admin.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * JDBC read model for administrator attempt-result reporting.
 *
 * <p>
 * Summary and detail rows are read from attempt snapshot tables and persisted
 * score columns. Detail rows join attempt questions to attempt answer options,
 * not live question-bank options, so reports reflect what the user saw. User
 * email is deliberately excluded, and this repository never recalculates
 * scores.
 * </p>
 */
@Repository
public class AdminResultQueryRepository {

    private static final String RESULT_SELECT = """
            SELECT
                a.id AS attempt_id,
                u.id AS user_id,
                u.full_name AS user_full_name,
                a.quiz_title_snapshot AS quiz_title,
                a.category_name_snapshot AS category_name,
                a.status,
                a.completion_reason,
                a.total_questions,
                a.correct_count,
                a.wrong_count,
                a.unanswered_count,
                a.score_percentage,
                a.started_at,
                a.expires_at,
                a.submitted_at,
                a.abandoned_at
            FROM quiz_attempts a
            JOIN users u ON u.id = a.user_id
            WHERE 1=1
            """;

    private static final String COUNT_SELECT = """
            SELECT COUNT(*)
            FROM quiz_attempts a
            JOIN users u ON u.id = a.user_id
            WHERE 1=1
            """;

    private static final String ATTEMPT_HEADER_SQL = """
            SELECT
                a.id AS attempt_id,
                u.id AS user_id,
                u.full_name AS user_full_name,
                a.quiz_id,
                a.quiz_title_snapshot AS quiz_title,
                a.category_id_snapshot AS category_id,
                a.category_name_snapshot AS category_name,
                a.status,
                a.completion_reason,
                a.total_questions,
                a.correct_count,
                a.wrong_count,
                a.unanswered_count,
                a.score_percentage,
                a.scoring_version,
                a.started_at,
                a.expires_at,
                a.submitted_at,
                a.abandoned_at
            FROM quiz_attempts a
            JOIN users u ON u.id = a.user_id
            WHERE a.id = :attemptId
            """;

    private static final String QUESTION_OPTION_SQL = """
            SELECT
                q.id AS attempt_question_id,
                q.original_question_id,
                q.question_text,
                q.display_order AS question_display_order,
                q.selected_option_id,
                q.correct AS question_correct,

                o.id AS attempt_answer_option_id,
                o.original_answer_option_id,
                o.option_text,
                o.correct AS option_correct,
                o.display_order AS option_display_order
            FROM attempt_questions q
            JOIN attempt_answer_options o ON o.attempt_question_id = q.id
            WHERE q.attempt_id = :attemptId
            ORDER BY q.display_order ASC, o.display_order ASC
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AdminResultQueryRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AdminResultSummaryRow> findResults(AdminResultSearchCriteria criteria) {
        QueryParts queryParts = buildFilteredQuery(RESULT_SELECT, criteria);
        String sql = queryParts.sql()
                + "ORDER BY a.started_at DESC, a.id DESC\n"
                + "LIMIT :limit OFFSET :offset\n";
        Map<String, Object> params = queryParts.params();
        params.put("limit", criteria.limit());
        params.put("offset", criteria.offset());
        return jdbcTemplate.query(sql, params, this::mapSummaryRow);
    }

    public long countResults(AdminResultSearchCriteria criteria) {
        QueryParts queryParts = buildFilteredQuery(COUNT_SELECT, criteria);
        Long count = jdbcTemplate.queryForObject(queryParts.sql(), queryParts.params(), Long.class);
        return count == null ? 0L : count;
    }

    public Optional<AdminResultAttemptRow> findAttemptHeader(Long attemptId) {
        List<AdminResultAttemptRow> rows = jdbcTemplate.query(
                ATTEMPT_HEADER_SQL,
                Map.of("attemptId", attemptId),
                this::mapAttemptRow);
        return rows.stream().findFirst();
    }

    public List<AdminResultQuestionOptionRow> findAttemptQuestionOptionRows(Long attemptId) {
        return jdbcTemplate.query(QUESTION_OPTION_SQL, Map.of("attemptId", attemptId), this::mapQuestionOptionRow);
    }

    private QueryParts buildFilteredQuery(String baseSql, AdminResultSearchCriteria criteria) {
        StringBuilder sql = new StringBuilder(baseSql);
        Map<String, Object> params = new HashMap<>();

        if (criteria.userId() != null) {
            sql.append("AND a.user_id = :userId\n");
            params.put("userId", criteria.userId());
        }
        if (criteria.quizId() != null) {
            sql.append("AND a.quiz_id = :quizId\n");
            params.put("quizId", criteria.quizId());
        }
        if (criteria.categoryId() != null) {
            sql.append("AND a.category_id_snapshot = :categoryId\n");
            params.put("categoryId", criteria.categoryId());
        }
        if (criteria.status() != null) {
            sql.append("AND a.status = :status\n");
            params.put("status", criteria.status().name());
        }
        if (criteria.startedFrom() != null) {
            sql.append("AND a.started_at >= :startedFrom\n");
            params.put("startedFrom", criteria.startedFrom());
        }
        if (criteria.startedToExclusive() != null) {
            sql.append("AND a.started_at < :startedToExclusive\n");
            params.put("startedToExclusive", criteria.startedToExclusive());
        }

        return new QueryParts(sql.toString(), params);
    }

    private AdminResultSummaryRow mapSummaryRow(ResultSet rs, int rowNum) throws SQLException {
        return new AdminResultSummaryRow(
                rs.getLong("attempt_id"),
                rs.getLong("user_id"),
                rs.getString("user_full_name"),
                rs.getString("quiz_title"),
                rs.getString("category_name"),
                rs.getString("status"),
                rs.getString("completion_reason"),
                rs.getInt("total_questions"),
                rs.getInt("correct_count"),
                rs.getInt("wrong_count"),
                rs.getInt("unanswered_count"),
                rs.getInt("score_percentage"),
                toInstant(rs.getTimestamp("started_at")),
                toInstant(rs.getTimestamp("expires_at")),
                toInstant(rs.getTimestamp("submitted_at")),
                toInstant(rs.getTimestamp("abandoned_at")));
    }

    private AdminResultAttemptRow mapAttemptRow(ResultSet rs, int rowNum) throws SQLException {
        return new AdminResultAttemptRow(
                rs.getLong("attempt_id"),
                rs.getLong("user_id"),
                rs.getString("user_full_name"),
                rs.getLong("quiz_id"),
                rs.getString("quiz_title"),
                rs.getLong("category_id"),
                rs.getString("category_name"),
                rs.getString("status"),
                rs.getString("completion_reason"),
                rs.getInt("total_questions"),
                rs.getInt("correct_count"),
                rs.getInt("wrong_count"),
                rs.getInt("unanswered_count"),
                rs.getInt("score_percentage"),
                rs.getString("scoring_version"),
                toInstant(rs.getTimestamp("started_at")),
                toInstant(rs.getTimestamp("expires_at")),
                toInstant(rs.getTimestamp("submitted_at")),
                toInstant(rs.getTimestamp("abandoned_at")));
    }

    private AdminResultQuestionOptionRow mapQuestionOptionRow(ResultSet rs, int rowNum) throws SQLException {
        return new AdminResultQuestionOptionRow(
                rs.getLong("attempt_question_id"),
                getNullableLong(rs, "original_question_id"),
                rs.getString("question_text"),
                rs.getInt("question_display_order"),
                getNullableLong(rs, "selected_option_id"),
                getNullableBoolean(rs, "question_correct"),
                rs.getLong("attempt_answer_option_id"),
                getNullableLong(rs, "original_answer_option_id"),
                rs.getString("option_text"),
                rs.getBoolean("option_correct"),
                rs.getInt("option_display_order"));
    }

    private Long getNullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private Boolean getNullableBoolean(ResultSet rs, String column) throws SQLException {
        boolean value = rs.getBoolean(column);
        return rs.wasNull() ? null : value;
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    private record QueryParts(String sql, Map<String, Object> params) {
    }

    public record AdminResultSummaryRow(
            Long attemptId,
            Long userId,
            String userFullName,
            String quizTitle,
            String categoryName,
            String status,
            String completionReason,
            int totalQuestions,
            int correctCount,
            int wrongCount,
            int unansweredCount,
            int scorePercentage,
            Instant startedAt,
            Instant expiresAt,
            Instant submittedAt,
            Instant abandonedAt) {
    }

    public record AdminResultAttemptRow(
            Long attemptId,
            Long userId,
            String userFullName,
            Long quizId,
            String quizTitle,
            Long categoryId,
            String categoryName,
            String status,
            String completionReason,
            int totalQuestions,
            int correctCount,
            int wrongCount,
            int unansweredCount,
            int scorePercentage,
            String scoringVersion,
            Instant startedAt,
            Instant expiresAt,
            Instant submittedAt,
            Instant abandonedAt) {
    }

    public record AdminResultQuestionOptionRow(
            Long attemptQuestionId,
            Long originalQuestionId,
            String questionText,
            int questionDisplayOrder,
            Long selectedOptionId,
            Boolean questionCorrect,
            Long attemptAnswerOptionId,
            Long originalAnswerOptionId,
            String optionText,
            boolean optionCorrect,
            int optionDisplayOrder) {
    }
}
