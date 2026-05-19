package com.quizz.leaderboard.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quizz.leaderboard.repository.LeaderboardQueryRepository.LeaderboardRow;
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
class LeaderboardQueryRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private ResultSet resultSet;

    private LeaderboardQueryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new LeaderboardQueryRepository(jdbcTemplate);
        when(jdbcTemplate.query(anyString(), anyMap(), anyRowMapper())).thenReturn(List.of());
    }

    @Test
    void findTopOverallPassesLimitAndUsesRequiredSqlShape() {
        repository.findTopOverall(25);

        CapturedQuery query = captureQuery();
        assertThat(query.params().get("limit")).isEqualTo(25);
        assertRequiredSqlShape(query.sql());
    }

    @Test
    void findTopByQuizPassesQuizIdAndLimit() {
        repository.findTopByQuiz(7L, 30);

        CapturedQuery query = captureQuery();
        assertThat(query.params().get("quizId")).isEqualTo(7L);
        assertThat(query.params().get("limit")).isEqualTo(30);
        assertRequiredSqlShape(query.sql());
        assertThat(query.sql()).contains("AND a.quiz_id = :quizId");
    }

    @Test
    void findTopByCategoryPassesCategoryIdAndLimit() {
        repository.findTopByCategory(4L, 15);

        CapturedQuery query = captureQuery();
        assertThat(query.params().get("categoryId")).isEqualTo(4L);
        assertThat(query.params().get("limit")).isEqualTo(15);
        assertRequiredSqlShape(query.sql());
        assertThat(query.sql()).contains("AND a.category_id_snapshot = :categoryId");
    }

    @Test
    void rowMapperMapsAllRequiredFields() throws Exception {
        Instant submittedAt = Instant.parse("2026-01-01T12:00:00Z");
        when(resultSet.getInt("rank_position")).thenReturn(1);
        when(resultSet.getLong("user_id")).thenReturn(2L);
        when(resultSet.getString("user_full_name")).thenReturn("Ada Lovelace");
        when(resultSet.getLong("quiz_id")).thenReturn(3L);
        when(resultSet.getString("quiz_title")).thenReturn("Science Quiz");
        when(resultSet.getLong("category_id")).thenReturn(4L);
        when(resultSet.getString("category_name")).thenReturn("Science");
        when(resultSet.getInt("total_questions")).thenReturn(5);
        when(resultSet.getInt("correct_count")).thenReturn(4);
        when(resultSet.getInt("score_percentage")).thenReturn(80);
        when(resultSet.getTimestamp("submitted_at")).thenReturn(Timestamp.from(submittedAt));
        repository.findTopOverall(10);

        LeaderboardRow row = captureQuery().rowMapper().mapRow(resultSet, 0);

        assertThat(row.rankPosition()).isEqualTo(1);
        assertThat(row.userId()).isEqualTo(2L);
        assertThat(row.userFullName()).isEqualTo("Ada Lovelace");
        assertThat(row.quizId()).isEqualTo(3L);
        assertThat(row.quizTitle()).isEqualTo("Science Quiz");
        assertThat(row.categoryId()).isEqualTo(4L);
        assertThat(row.categoryName()).isEqualTo("Science");
        assertThat(row.totalQuestions()).isEqualTo(5);
        assertThat(row.correctCount()).isEqualTo(4);
        assertThat(row.scorePercentage()).isEqualTo(80);
        assertThat(row.submittedAt()).isEqualTo(submittedAt);
    }

    private void assertRequiredSqlShape(String sql) {
        assertThat(sql).contains("a.status = 'COMPLETED'");
        assertThat(sql).contains("a.submitted_at IS NOT NULL");
        assertThat(sql).contains("ROW_NUMBER() OVER");
        assertThat(sql).contains("PARTITION BY a.user_id");
        assertThat(sql).doesNotContain("email");
        assertThat(sql).contains("score_percentage DESC");
        assertThat(sql).contains("correct_count DESC");
        assertThat(sql).contains("submitted_at ASC");
        assertThat(sql).contains("id ASC");
        assertThat(sql).contains("LIMIT :limit");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private CapturedQuery captureQuery() {
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, ?>> paramsCaptor = ArgumentCaptor.forClass((Class) Map.class);
        ArgumentCaptor<RowMapper<LeaderboardRow>> rowMapperCaptor = ArgumentCaptor.forClass((Class) RowMapper.class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), paramsCaptor.capture(), rowMapperCaptor.capture());
        return new CapturedQuery(sqlCaptor.getValue(), paramsCaptor.getValue(), rowMapperCaptor.getValue());
    }

    private record CapturedQuery(
            String sql,
            Map<String, ?> params,
            RowMapper<LeaderboardRow> rowMapper) {
    }

    private static <T> RowMapper<T> anyRowMapper() {
        return any();
    }
}
