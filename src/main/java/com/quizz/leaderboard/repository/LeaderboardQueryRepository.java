package com.quizz.leaderboard.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * JDBC read model for public leaderboard rankings.
 *
 * <p>
 * There is no leaderboard table or entity. Queries rank completed attempts
 * with non-null {@code submitted_at}, keep one best row per user within the
 * selected view, and use deterministic tie-breakers. User email is deliberately
 * excluded from the select list.
 * </p>
 */
@Repository
public class LeaderboardQueryRepository {

    private static final String OVERALL_SQL = """
            WITH eligible_attempts AS (
                SELECT
                    a.id,
                    a.user_id,
                    a.quiz_id,
                    a.quiz_title_snapshot,
                    a.category_id_snapshot,
                    a.category_name_snapshot,
                    a.total_questions,
                    a.correct_count,
                    a.score_percentage,
                    a.submitted_at,
                    ROW_NUMBER() OVER (
                        PARTITION BY a.user_id
                        ORDER BY
                            a.score_percentage DESC,
                            a.correct_count DESC,
                            a.submitted_at ASC,
                            a.id ASC
                    ) AS user_attempt_rank
                FROM quiz_attempts a
                WHERE a.status = 'COMPLETED'
                  AND a.submitted_at IS NOT NULL
            ),
            best_attempts AS (
                SELECT *
                FROM eligible_attempts
                WHERE user_attempt_rank = 1
            ),
            ranked AS (
                SELECT
                    ROW_NUMBER() OVER (
                        ORDER BY
                            score_percentage DESC,
                            correct_count DESC,
                            submitted_at ASC,
                            id ASC
                    ) AS rank_position,
                    *
                FROM best_attempts
            )
            SELECT
                r.rank_position,
                r.user_id,
                u.full_name AS user_full_name,
                r.quiz_id,
                r.quiz_title_snapshot AS quiz_title,
                r.category_id_snapshot AS category_id,
                r.category_name_snapshot AS category_name,
                r.total_questions,
                r.correct_count,
                r.score_percentage,
                r.submitted_at
            FROM ranked r
            JOIN users u ON u.id = r.user_id
            ORDER BY r.rank_position
            LIMIT :limit
            """;

    private static final String QUIZ_SQL = """
            WITH eligible_attempts AS (
                SELECT
                    a.id,
                    a.user_id,
                    a.quiz_id,
                    a.quiz_title_snapshot,
                    a.category_id_snapshot,
                    a.category_name_snapshot,
                    a.total_questions,
                    a.correct_count,
                    a.score_percentage,
                    a.submitted_at,
                    ROW_NUMBER() OVER (
                        PARTITION BY a.user_id
                        ORDER BY
                            a.score_percentage DESC,
                            a.correct_count DESC,
                            a.submitted_at ASC,
                            a.id ASC
                    ) AS user_attempt_rank
                FROM quiz_attempts a
                WHERE a.status = 'COMPLETED'
                  AND a.submitted_at IS NOT NULL
                  AND a.quiz_id = :quizId
            ),
            best_attempts AS (
                SELECT *
                FROM eligible_attempts
                WHERE user_attempt_rank = 1
            ),
            ranked AS (
                SELECT
                    ROW_NUMBER() OVER (
                        ORDER BY
                            score_percentage DESC,
                            correct_count DESC,
                            submitted_at ASC,
                            id ASC
                    ) AS rank_position,
                    *
                FROM best_attempts
            )
            SELECT
                r.rank_position,
                r.user_id,
                u.full_name AS user_full_name,
                r.quiz_id,
                r.quiz_title_snapshot AS quiz_title,
                r.category_id_snapshot AS category_id,
                r.category_name_snapshot AS category_name,
                r.total_questions,
                r.correct_count,
                r.score_percentage,
                r.submitted_at
            FROM ranked r
            JOIN users u ON u.id = r.user_id
            ORDER BY r.rank_position
            LIMIT :limit
            """;

    private static final String CATEGORY_SQL = """
            WITH eligible_attempts AS (
                SELECT
                    a.id,
                    a.user_id,
                    a.quiz_id,
                    a.quiz_title_snapshot,
                    a.category_id_snapshot,
                    a.category_name_snapshot,
                    a.total_questions,
                    a.correct_count,
                    a.score_percentage,
                    a.submitted_at,
                    ROW_NUMBER() OVER (
                        PARTITION BY a.user_id
                        ORDER BY
                            a.score_percentage DESC,
                            a.correct_count DESC,
                            a.submitted_at ASC,
                            a.id ASC
                    ) AS user_attempt_rank
                FROM quiz_attempts a
                WHERE a.status = 'COMPLETED'
                  AND a.submitted_at IS NOT NULL
                  AND a.category_id_snapshot = :categoryId
            ),
            best_attempts AS (
                SELECT *
                FROM eligible_attempts
                WHERE user_attempt_rank = 1
            ),
            ranked AS (
                SELECT
                    ROW_NUMBER() OVER (
                        ORDER BY
                            score_percentage DESC,
                            correct_count DESC,
                            submitted_at ASC,
                            id ASC
                    ) AS rank_position,
                    *
                FROM best_attempts
            )
            SELECT
                r.rank_position,
                r.user_id,
                u.full_name AS user_full_name,
                r.quiz_id,
                r.quiz_title_snapshot AS quiz_title,
                r.category_id_snapshot AS category_id,
                r.category_name_snapshot AS category_name,
                r.total_questions,
                r.correct_count,
                r.score_percentage,
                r.submitted_at
            FROM ranked r
            JOIN users u ON u.id = r.user_id
            ORDER BY r.rank_position
            LIMIT :limit
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public LeaderboardQueryRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<LeaderboardRow> findTopOverall(int limit) {
        return jdbcTemplate.query(OVERALL_SQL, Map.of("limit", limit), this::mapRow);
    }

    public List<LeaderboardRow> findTopByQuiz(Long quizId, int limit) {
        return jdbcTemplate.query(QUIZ_SQL, Map.of("quizId", quizId, "limit", limit), this::mapRow);
    }

    public List<LeaderboardRow> findTopByCategory(Long categoryId, int limit) {
        return jdbcTemplate.query(CATEGORY_SQL, Map.of("categoryId", categoryId, "limit", limit), this::mapRow);
    }

    private LeaderboardRow mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new LeaderboardRow(
                rs.getInt("rank_position"),
                rs.getLong("user_id"),
                rs.getString("user_full_name"),
                rs.getLong("quiz_id"),
                rs.getString("quiz_title"),
                rs.getLong("category_id"),
                rs.getString("category_name"),
                rs.getInt("total_questions"),
                rs.getInt("correct_count"),
                rs.getInt("score_percentage"),
                rs.getTimestamp("submitted_at").toInstant());
    }

    public record LeaderboardRow(
            int rankPosition,
            Long userId,
            String userFullName,
            Long quizId,
            String quizTitle,
            Long categoryId,
            String categoryName,
            int totalQuestions,
            int correctCount,
            int scorePercentage,
            Instant submittedAt) {
    }
}
