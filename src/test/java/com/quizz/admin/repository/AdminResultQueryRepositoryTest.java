package com.quizz.admin.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quizz.admin.repository.AdminResultQueryRepository.AdminResultAttemptRow;
import com.quizz.admin.repository.AdminResultQueryRepository.AdminResultQuestionOptionRow;
import com.quizz.admin.repository.AdminResultQueryRepository.AdminResultSummaryRow;
import com.quizz.attempt.entity.AttemptStatus;
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
class AdminResultQueryRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private ResultSet resultSet;

    private AdminResultQueryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new AdminResultQueryRepository(jdbcTemplate);
        lenient().when(jdbcTemplate.query(anyString(), anyMap(), anyRowMapper())).thenReturn(List.of());
        lenient().when(jdbcTemplate.queryForObject(anyString(), anyMap(), eq(Long.class))).thenReturn(0L);
    }

    @Test
    void findResultsAppliesLimitOffsetAndOptionalFilters() {
        AdminResultSearchCriteria criteria = fullCriteria();

        repository.findResults(criteria);

        CapturedQuery<AdminResultSummaryRow> query = captureQuery();
        assertThat(query.params().get("limit")).isEqualTo(25);
        assertThat(query.params().get("offset")).isEqualTo(50);
        assertThat(query.params().get("userId")).isEqualTo(1L);
        assertThat(query.params().get("quizId")).isEqualTo(2L);
        assertThat(query.params().get("categoryId")).isEqualTo(3L);
        assertThat(query.params().get("status")).isEqualTo("COMPLETED");
        assertThat(query.params().get("startedFrom")).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
        assertThat(query.params().get("startedToExclusive")).isEqualTo(Instant.parse("2026-02-01T00:00:00Z"));
        assertThat(query.sql()).contains("AND a.user_id = :userId");
        assertThat(query.sql()).contains("AND a.quiz_id = :quizId");
        assertThat(query.sql()).contains("AND a.category_id_snapshot = :categoryId");
        assertThat(query.sql()).contains("AND a.status = :status");
        assertThat(query.sql()).contains("AND a.started_at >= :startedFrom");
        assertThat(query.sql()).contains("AND a.started_at < :startedToExclusive");
        assertThat(query.sql()).contains("ORDER BY a.started_at DESC, a.id DESC");
        assertThat(query.sql()).contains("LIMIT :limit OFFSET :offset");
        assertThat(query.sql()).doesNotContain("email");
    }

    @Test
    void countResultsUsesSameFiltersWithoutLimitOrOffset() {
        repository.countResults(fullCriteria());

        CapturedCountQuery query = captureCountQuery();
        assertThat(query.params().get("userId")).isEqualTo(1L);
        assertThat(query.params().get("quizId")).isEqualTo(2L);
        assertThat(query.params().get("categoryId")).isEqualTo(3L);
        assertThat(query.params().get("status")).isEqualTo("COMPLETED");
        assertThat(query.sql()).contains("SELECT COUNT(*)");
        assertThat(query.sql()).contains("AND a.user_id = :userId");
        assertThat(query.sql()).contains("AND a.quiz_id = :quizId");
        assertThat(query.sql()).contains("AND a.category_id_snapshot = :categoryId");
        assertThat(query.sql()).contains("AND a.status = :status");
        assertThat(query.sql()).contains("AND a.started_at >= :startedFrom");
        assertThat(query.sql()).contains("AND a.started_at < :startedToExclusive");
        assertThat(query.sql()).doesNotContain("LIMIT");
        assertThat(query.sql()).doesNotContain("OFFSET");
    }

    @Test
    void findAttemptHeaderUsesAttemptIdAndDoesNotSelectEmail() {
        repository.findAttemptHeader(7L);

        CapturedQuery<AdminResultAttemptRow> query = captureQuery();
        assertThat(query.params().get("attemptId")).isEqualTo(7L);
        assertThat(query.sql()).contains("WHERE a.id = :attemptId");
        assertThat(query.sql()).doesNotContain("email");
    }

    @Test
    void findAttemptQuestionOptionRowsUsesSnapshotTablesOnly() {
        repository.findAttemptQuestionOptionRows(7L);

        CapturedQuery<AdminResultQuestionOptionRow> query = captureQuery();
        assertThat(query.params().get("attemptId")).isEqualTo(7L);
        assertThat(query.sql()).contains("FROM attempt_questions q");
        assertThat(query.sql()).contains("JOIN attempt_answer_options o ON o.attempt_question_id = q.id");
        assertThat(query.sql()).contains("ORDER BY q.display_order ASC, o.display_order ASC");
        assertThat(query.sql()).doesNotContain("JOIN questions");
        assertThat(query.sql()).doesNotContain("JOIN answer_options");
        assertThat(query.sql()).doesNotContain("FROM questions");
        assertThat(query.sql()).doesNotContain("FROM answer_options");
    }

    @Test
    void summaryRowMapperMapsFields() throws Exception {
        Instant startedAt = Instant.parse("2026-01-01T10:00:00Z");
        Instant expiresAt = Instant.parse("2026-01-01T10:30:00Z");
        Instant submittedAt = Instant.parse("2026-01-01T10:15:00Z");
        stubSummaryResultSet(startedAt, expiresAt, submittedAt);

        repository.findResults(new AdminResultSearchCriteria(null, null, null, null, null, null, 20, 0));
        AdminResultSummaryRow row = this.<AdminResultSummaryRow>captureQuery().rowMapper().mapRow(resultSet, 0);

        assertThat(row.attemptId()).isEqualTo(1L);
        assertThat(row.userId()).isEqualTo(2L);
        assertThat(row.userFullName()).isEqualTo("Ada Lovelace");
        assertThat(row.quizTitle()).isEqualTo("Science Quiz");
        assertThat(row.categoryName()).isEqualTo("Science");
        assertThat(row.status()).isEqualTo("COMPLETED");
        assertThat(row.totalQuestions()).isEqualTo(5);
        assertThat(row.correctCount()).isEqualTo(4);
        assertThat(row.wrongCount()).isEqualTo(1);
        assertThat(row.unansweredCount()).isZero();
        assertThat(row.scorePercentage()).isEqualTo(80);
        assertThat(row.startedAt()).isEqualTo(startedAt);
        assertThat(row.expiresAt()).isEqualTo(expiresAt);
        assertThat(row.submittedAt()).isEqualTo(submittedAt);
    }

    @Test
    void headerRowMapperMapsFields() throws Exception {
        Instant startedAt = Instant.parse("2026-01-01T10:00:00Z");
        Instant expiresAt = Instant.parse("2026-01-01T10:30:00Z");
        when(resultSet.getLong("attempt_id")).thenReturn(1L);
        when(resultSet.getLong("user_id")).thenReturn(2L);
        when(resultSet.getString("user_full_name")).thenReturn("Ada Lovelace");
        when(resultSet.getLong("quiz_id")).thenReturn(3L);
        when(resultSet.getString("quiz_title")).thenReturn("Science Quiz");
        when(resultSet.getLong("category_id")).thenReturn(4L);
        when(resultSet.getString("category_name")).thenReturn("Science");
        when(resultSet.getString("status")).thenReturn("IN_PROGRESS");
        when(resultSet.getString("completion_reason")).thenReturn(null);
        when(resultSet.getInt("total_questions")).thenReturn(5);
        when(resultSet.getInt("correct_count")).thenReturn(0);
        when(resultSet.getInt("wrong_count")).thenReturn(0);
        when(resultSet.getInt("unanswered_count")).thenReturn(5);
        when(resultSet.getInt("score_percentage")).thenReturn(0);
        when(resultSet.getString("scoring_version")).thenReturn("v1");
        when(resultSet.getTimestamp("started_at")).thenReturn(Timestamp.from(startedAt));
        when(resultSet.getTimestamp("expires_at")).thenReturn(Timestamp.from(expiresAt));
        when(resultSet.getTimestamp("submitted_at")).thenReturn(null);
        when(resultSet.getTimestamp("abandoned_at")).thenReturn(null);

        repository.findAttemptHeader(1L);
        AdminResultAttemptRow row = this.<AdminResultAttemptRow>captureQuery().rowMapper().mapRow(resultSet, 0);

        assertThat(row.attemptId()).isEqualTo(1L);
        assertThat(row.userId()).isEqualTo(2L);
        assertThat(row.quizId()).isEqualTo(3L);
        assertThat(row.categoryId()).isEqualTo(4L);
        assertThat(row.scoringVersion()).isEqualTo("v1");
        assertThat(row.startedAt()).isEqualTo(startedAt);
        assertThat(row.expiresAt()).isEqualTo(expiresAt);
        assertThat(row.submittedAt()).isNull();
    }

    @Test
    void detailRowMapperMapsSnapshotFields() throws Exception {
        when(resultSet.getLong("attempt_question_id")).thenReturn(10L);
        when(resultSet.getLong("original_question_id")).thenReturn(100L);
        when(resultSet.getString("question_text")).thenReturn("Question?");
        when(resultSet.getInt("question_display_order")).thenReturn(1);
        when(resultSet.getLong("selected_option_id")).thenReturn(201L);
        when(resultSet.getBoolean("question_correct")).thenReturn(true);
        when(resultSet.getLong("attempt_answer_option_id")).thenReturn(201L);
        when(resultSet.getLong("original_answer_option_id")).thenReturn(301L);
        when(resultSet.getString("option_text")).thenReturn("Correct");
        when(resultSet.getBoolean("option_correct")).thenReturn(true);
        when(resultSet.getInt("option_display_order")).thenReturn(2);
        when(resultSet.wasNull()).thenReturn(false, false, false, false);

        repository.findAttemptQuestionOptionRows(1L);
        AdminResultQuestionOptionRow row = this.<AdminResultQuestionOptionRow>captureQuery()
                .rowMapper()
                .mapRow(resultSet, 0);

        assertThat(row.attemptQuestionId()).isEqualTo(10L);
        assertThat(row.originalQuestionId()).isEqualTo(100L);
        assertThat(row.questionText()).isEqualTo("Question?");
        assertThat(row.questionDisplayOrder()).isEqualTo(1);
        assertThat(row.selectedOptionId()).isEqualTo(201L);
        assertThat(row.questionCorrect()).isTrue();
        assertThat(row.attemptAnswerOptionId()).isEqualTo(201L);
        assertThat(row.originalAnswerOptionId()).isEqualTo(301L);
        assertThat(row.optionText()).isEqualTo("Correct");
        assertThat(row.optionCorrect()).isTrue();
        assertThat(row.optionDisplayOrder()).isEqualTo(2);
    }

    private AdminResultSearchCriteria fullCriteria() {
        return new AdminResultSearchCriteria(
                1L,
                2L,
                3L,
                AttemptStatus.COMPLETED,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"),
                25,
                50
        );
    }

    private void stubSummaryResultSet(Instant startedAt, Instant expiresAt, Instant submittedAt) throws Exception {
        when(resultSet.getLong("attempt_id")).thenReturn(1L);
        when(resultSet.getLong("user_id")).thenReturn(2L);
        when(resultSet.getString("user_full_name")).thenReturn("Ada Lovelace");
        when(resultSet.getString("quiz_title")).thenReturn("Science Quiz");
        when(resultSet.getString("category_name")).thenReturn("Science");
        when(resultSet.getString("status")).thenReturn("COMPLETED");
        when(resultSet.getString("completion_reason")).thenReturn("MANUAL");
        when(resultSet.getInt("total_questions")).thenReturn(5);
        when(resultSet.getInt("correct_count")).thenReturn(4);
        when(resultSet.getInt("wrong_count")).thenReturn(1);
        when(resultSet.getInt("unanswered_count")).thenReturn(0);
        when(resultSet.getInt("score_percentage")).thenReturn(80);
        when(resultSet.getTimestamp("started_at")).thenReturn(Timestamp.from(startedAt));
        when(resultSet.getTimestamp("expires_at")).thenReturn(Timestamp.from(expiresAt));
        when(resultSet.getTimestamp("submitted_at")).thenReturn(Timestamp.from(submittedAt));
        when(resultSet.getTimestamp("abandoned_at")).thenReturn(null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> CapturedQuery<T> captureQuery() {
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, ?>> paramsCaptor = ArgumentCaptor.forClass((Class) Map.class);
        ArgumentCaptor<RowMapper<T>> rowMapperCaptor = ArgumentCaptor.forClass((Class) RowMapper.class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), paramsCaptor.capture(), rowMapperCaptor.capture());
        return new CapturedQuery<>(sqlCaptor.getValue(), paramsCaptor.getValue(), rowMapperCaptor.getValue());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private CapturedCountQuery captureCountQuery() {
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, ?>> paramsCaptor = ArgumentCaptor.forClass((Class) Map.class);
        verify(jdbcTemplate).queryForObject(sqlCaptor.capture(), paramsCaptor.capture(), eq(Long.class));
        return new CapturedCountQuery(sqlCaptor.getValue(), paramsCaptor.getValue());
    }

    private record CapturedQuery<T>(
            String sql,
            Map<String, ?> params,
            RowMapper<T> rowMapper
    ) {
    }

    private record CapturedCountQuery(
            String sql,
            Map<String, ?> params
    ) {
    }

    private static <T> RowMapper<T> anyRowMapper() {
        return any();
    }
}
