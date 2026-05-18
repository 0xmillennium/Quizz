package com.quizz.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quizz.admin.dto.AdminResultDetailResponse;
import com.quizz.admin.dto.AdminResultFilterRequest;
import com.quizz.admin.dto.AdminResultListResponse;
import com.quizz.admin.repository.AdminResultQueryRepository;
import com.quizz.admin.repository.AdminResultQueryRepository.AdminResultAttemptRow;
import com.quizz.admin.repository.AdminResultQueryRepository.AdminResultQuestionOptionRow;
import com.quizz.admin.repository.AdminResultQueryRepository.AdminResultSummaryRow;
import com.quizz.admin.repository.AdminResultSearchCriteria;
import com.quizz.attempt.entity.AttemptStatus;
import com.quizz.category.service.CategoryQueryService;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.common.exception.NotFoundException;
import com.quizz.quiz.service.QuizQueryService;
import com.quizz.user.service.UserQueryService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultAdminResultServiceTest {

    @Mock
    private AdminResultQueryRepository repository;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private QuizQueryService quizQueryService;

    @Mock
    private CategoryQueryService categoryQueryService;

    private DefaultAdminResultService service;

    @BeforeEach
    void setUp() {
        service = new DefaultAdminResultService(
                repository,
                userQueryService,
                quizQueryService,
                categoryQueryService
        );
        lenient().when(repository.countResults(org.mockito.ArgumentMatchers.any(AdminResultSearchCriteria.class))).thenReturn(0L);
        lenient().when(repository.findResults(org.mockito.ArgumentMatchers.any(AdminResultSearchCriteria.class))).thenReturn(List.of());
    }

    @Test
    void searchResultsWithNullFilterUsesDefaultPageAndSize() {
        AdminResultListResponse response = service.searchResults(null);

        AdminResultSearchCriteria criteria = capturedFindCriteria();
        assertThat(criteria.limit()).isEqualTo(20);
        assertThat(criteria.offset()).isZero();
        assertThat(response.page().page()).isEqualTo(1);
        assertThat(response.page().size()).isEqualTo(20);
    }

    @Test
    void searchResultsNormalizesInvalidPageAndSizeValues() {
        AdminResultFilterRequest filter = new AdminResultFilterRequest();
        filter.setPage(0);
        filter.setSize(0);

        AdminResultListResponse response = service.searchResults(filter);

        assertThat(response.page().page()).isEqualTo(1);
        assertThat(response.page().size()).isEqualTo(20);
    }

    @Test
    void searchResultsNormalizesNullSizeToDefault() {
        AdminResultFilterRequest filter = new AdminResultFilterRequest();
        filter.setPage(2);

        service.searchResults(filter);

        AdminResultSearchCriteria criteria = capturedFindCriteria();
        assertThat(criteria.limit()).isEqualTo(20);
        assertThat(criteria.offset()).isEqualTo(20);
    }

    @Test
    void searchResultsCapsSizeAboveOneHundred() {
        AdminResultFilterRequest filter = new AdminResultFilterRequest();
        filter.setSize(101);

        service.searchResults(filter);

        assertThat(capturedFindCriteria().limit()).isEqualTo(100);
    }

    @Test
    void searchResultsPreservesValidSize() {
        AdminResultFilterRequest filter = new AdminResultFilterRequest();
        filter.setPage(3);
        filter.setSize(50);

        service.searchResults(filter);

        AdminResultSearchCriteria criteria = capturedFindCriteria();
        assertThat(criteria.limit()).isEqualTo(50);
        assertThat(criteria.offset()).isEqualTo(100);
    }

    @Test
    void searchResultsNormalizesValidStatusToUppercase() {
        AdminResultFilterRequest filter = new AdminResultFilterRequest();
        filter.setStatus(" completed ");

        AdminResultListResponse response = service.searchResults(filter);

        AdminResultSearchCriteria criteria = capturedFindCriteria();
        assertThat(criteria.status()).isEqualTo(AttemptStatus.COMPLETED);
        assertThat(response.filter().getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void searchResultsInvalidStatusThrowsBusinessRuleException() {
        AdminResultFilterRequest filter = new AdminResultFilterRequest();
        filter.setStatus("started");

        assertThatThrownBy(() -> service.searchResults(filter))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Invalid attempt status.");
    }

    @Test
    void searchResultsStartedFromAfterStartedToThrowsBusinessRuleException() {
        AdminResultFilterRequest filter = new AdminResultFilterRequest();
        filter.setStartedFrom(LocalDate.of(2026, 1, 2));
        filter.setStartedTo(LocalDate.of(2026, 1, 1));

        assertThatThrownBy(() -> service.searchResults(filter))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Invalid date range.");
    }

    @Test
    void searchResultsValidatesReferencedFiltersAndConvertsDatesInUtc() {
        AdminResultFilterRequest filter = new AdminResultFilterRequest();
        filter.setUserId(1L);
        filter.setQuizId(2L);
        filter.setCategoryId(3L);
        filter.setStartedFrom(LocalDate.of(2026, 1, 1));
        filter.setStartedTo(LocalDate.of(2026, 1, 31));

        service.searchResults(filter);

        verify(userQueryService).getById(1L);
        verify(quizQueryService).getById(2L);
        verify(categoryQueryService).getById(3L);
        AdminResultSearchCriteria criteria = capturedFindCriteria();
        assertThat(criteria.startedFrom()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
        assertThat(criteria.startedToExclusive()).isEqualTo(Instant.parse("2026-02-01T00:00:00Z"));
    }

    @Test
    void searchResultsCallsCountAndFindMapsRowsAndComputesTotalPages() {
        Instant startedAt = Instant.parse("2026-01-01T10:00:00Z");
        Instant expiresAt = Instant.parse("2026-01-01T10:30:00Z");
        Instant submittedAt = Instant.parse("2026-01-01T10:15:00Z");
        AdminResultSummaryRow row = new AdminResultSummaryRow(
                10L,
                20L,
                "Ada Lovelace",
                "Science Quiz",
                "Science",
                "COMPLETED",
                "MANUAL",
                5,
                4,
                1,
                0,
                80,
                startedAt,
                expiresAt,
                submittedAt,
                null
        );
        AdminResultFilterRequest filter = new AdminResultFilterRequest();
        filter.setSize(20);
        when(repository.countResults(org.mockito.ArgumentMatchers.any(AdminResultSearchCriteria.class))).thenReturn(41L);
        when(repository.findResults(org.mockito.ArgumentMatchers.any(AdminResultSearchCriteria.class))).thenReturn(List.of(row));

        AdminResultListResponse response = service.searchResults(filter);

        verify(repository).countResults(org.mockito.ArgumentMatchers.any(AdminResultSearchCriteria.class));
        verify(repository).findResults(org.mockito.ArgumentMatchers.any(AdminResultSearchCriteria.class));
        assertThat(response.page().totalItems()).isEqualTo(41);
        assertThat(response.page().totalPages()).isEqualTo(3);
        assertThat(response.page().hasPrevious()).isFalse();
        assertThat(response.page().hasNext()).isTrue();
        assertThat(response.results()).singleElement().satisfies(result -> {
            assertThat(result.attemptId()).isEqualTo(10L);
            assertThat(result.userId()).isEqualTo(20L);
            assertThat(result.userFullName()).isEqualTo("Ada Lovelace");
            assertThat(result.quizTitle()).isEqualTo("Science Quiz");
            assertThat(result.categoryName()).isEqualTo("Science");
            assertThat(result.status()).isEqualTo("COMPLETED");
            assertThat(result.totalQuestions()).isEqualTo(5);
            assertThat(result.correctCount()).isEqualTo(4);
            assertThat(result.wrongCount()).isEqualTo(1);
            assertThat(result.unansweredCount()).isZero();
            assertThat(result.scorePercentage()).isEqualTo(80);
            assertThat(result.startedAt()).isEqualTo(startedAt);
            assertThat(result.expiresAt()).isEqualTo(expiresAt);
            assertThat(result.submittedAt()).isEqualTo(submittedAt);
        });
    }

    @Test
    void getResultDetailMissingAttemptThrowsNotFoundException() {
        when(repository.findAttemptHeader(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getResultDetail(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Attempt not found.");
    }

    @Test
    void getResultDetailGroupsQuestionOptionRowsAndMarksSelectedOption() {
        AdminResultAttemptRow header = new AdminResultAttemptRow(
                1L,
                2L,
                "Ada Lovelace",
                3L,
                "Science Quiz",
                4L,
                "Science",
                "COMPLETED",
                "MANUAL",
                1,
                1,
                0,
                0,
                100,
                "v1",
                Instant.parse("2026-01-01T10:00:00Z"),
                Instant.parse("2026-01-01T10:30:00Z"),
                Instant.parse("2026-01-01T10:10:00Z"),
                null
        );
        when(repository.findAttemptHeader(1L)).thenReturn(Optional.of(header));
        when(repository.findAttemptQuestionOptionRows(1L)).thenReturn(List.of(
                new AdminResultQuestionOptionRow(10L, 100L, "Question?", 1, 201L, true,
                        200L, 300L, "Wrong", false, 1),
                new AdminResultQuestionOptionRow(10L, 100L, "Question?", 1, 201L, true,
                        201L, 301L, "Correct", true, 2)
        ));

        AdminResultDetailResponse response = service.getResultDetail(1L);

        assertThat(response.attemptId()).isEqualTo(1L);
        assertThat(response.userFullName()).isEqualTo("Ada Lovelace");
        assertThat(response.questions()).singleElement().satisfies(question -> {
            assertThat(question.attemptQuestionId()).isEqualTo(10L);
            assertThat(question.originalQuestionId()).isEqualTo(100L);
            assertThat(question.questionText()).isEqualTo("Question?");
            assertThat(question.displayOrder()).isEqualTo(1);
            assertThat(question.selectedOptionId()).isEqualTo(201L);
            assertThat(question.correct()).isTrue();
            assertThat(question.options()).hasSize(2);
            assertThat(question.options().get(0).selected()).isFalse();
            assertThat(question.options().get(1).selected()).isTrue();
            assertThat(question.options().get(1).correct()).isTrue();
        });
    }

    private AdminResultSearchCriteria capturedFindCriteria() {
        ArgumentCaptor<AdminResultSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(AdminResultSearchCriteria.class);
        verify(repository).findResults(criteriaCaptor.capture());
        return criteriaCaptor.getValue();
    }
}
