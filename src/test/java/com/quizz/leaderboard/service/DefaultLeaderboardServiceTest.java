package com.quizz.leaderboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quizz.category.service.CategoryQueryService;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.leaderboard.dto.LeaderboardEntryResponse;
import com.quizz.leaderboard.dto.LeaderboardFilterRequest;
import com.quizz.leaderboard.dto.LeaderboardViewResponse;
import com.quizz.leaderboard.repository.LeaderboardQueryRepository;
import com.quizz.leaderboard.repository.LeaderboardQueryRepository.LeaderboardRow;
import com.quizz.quiz.service.QuizQueryService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultLeaderboardServiceTest {

    @Mock
    private LeaderboardQueryRepository leaderboardQueryRepository;

    @Mock
    private QuizQueryService quizQueryService;

    @Mock
    private CategoryQueryService categoryQueryService;

    private DefaultLeaderboardService service;

    @BeforeEach
    void setUp() {
        service = new DefaultLeaderboardService(
                leaderboardQueryRepository,
                quizQueryService,
                categoryQueryService);
    }

    @Test
    void getLeaderboardWithNullFilterReturnsOverallWithDefaultLimit() {
        when(leaderboardQueryRepository.findTopOverall(10)).thenReturn(List.of());

        LeaderboardViewResponse response = service.getLeaderboard(null);

        assertThat(response.scope()).isEqualTo("OVERALL");
        assertThat(response.limit()).isEqualTo(10);
        assertThat(response.entries()).isEmpty();
    }

    @Test
    void getLeaderboardWithEmptyFilterReturnsOverall() {
        when(leaderboardQueryRepository.findTopOverall(10)).thenReturn(List.of());

        LeaderboardViewResponse response = service.getLeaderboard(new LeaderboardFilterRequest());

        assertThat(response.scope()).isEqualTo("OVERALL");
        verify(leaderboardQueryRepository).findTopOverall(10);
    }

    @Test
    void limitNullDefaultsToTen() {
        LeaderboardFilterRequest filter = new LeaderboardFilterRequest();
        when(leaderboardQueryRepository.findTopOverall(10)).thenReturn(List.of());

        assertThat(service.getLeaderboard(filter).limit()).isEqualTo(10);
    }

    @Test
    void limitBelowOneDefaultsToTen() {
        LeaderboardFilterRequest filter = new LeaderboardFilterRequest();
        filter.setLimit(0);
        when(leaderboardQueryRepository.findTopOverall(10)).thenReturn(List.of());

        assertThat(service.getLeaderboard(filter).limit()).isEqualTo(10);
    }

    @Test
    void limitAboveOneHundredIsCapped() {
        LeaderboardFilterRequest filter = new LeaderboardFilterRequest();
        filter.setLimit(101);
        when(leaderboardQueryRepository.findTopOverall(100)).thenReturn(List.of());

        assertThat(service.getLeaderboard(filter).limit()).isEqualTo(100);
    }

    @Test
    void validLimitIsPreserved() {
        LeaderboardFilterRequest filter = new LeaderboardFilterRequest();
        filter.setLimit(25);
        when(leaderboardQueryRepository.findTopOverall(25)).thenReturn(List.of());

        assertThat(service.getLeaderboard(filter).limit()).isEqualTo(25);
    }

    @Test
    void quizIdFilterValidatesQuizExistsAndQueriesQuizLeaderboard() {
        LeaderboardFilterRequest filter = new LeaderboardFilterRequest();
        filter.setQuizId(7L);
        filter.setLimit(15);
        when(leaderboardQueryRepository.findTopByQuiz(7L, 15)).thenReturn(List.of());

        LeaderboardViewResponse response = service.getLeaderboard(filter);

        assertThat(response.scope()).isEqualTo("QUIZ");
        assertThat(response.quizId()).isEqualTo(7L);
        verify(quizQueryService).getById(7L);
        verify(leaderboardQueryRepository).findTopByQuiz(7L, 15);
    }

    @Test
    void categoryIdFilterValidatesCategoryExistsAndQueriesCategoryLeaderboard() {
        LeaderboardFilterRequest filter = new LeaderboardFilterRequest();
        filter.setCategoryId(4L);
        filter.setLimit(20);
        when(leaderboardQueryRepository.findTopByCategory(4L, 20)).thenReturn(List.of());

        LeaderboardViewResponse response = service.getLeaderboard(filter);

        assertThat(response.scope()).isEqualTo("CATEGORY");
        assertThat(response.categoryId()).isEqualTo(4L);
        verify(categoryQueryService).getById(4L);
        verify(leaderboardQueryRepository).findTopByCategory(4L, 20);
    }

    @Test
    void quizIdAndCategoryIdTogetherThrowsBusinessRuleException() {
        LeaderboardFilterRequest filter = new LeaderboardFilterRequest();
        filter.setQuizId(7L);
        filter.setCategoryId(4L);

        assertThatThrownBy(() -> service.getLeaderboard(filter))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Select either quiz or category filter, not both.");
    }

    @Test
    void repositoryRowsAreMappedToEntryResponses() {
        Instant submittedAt = Instant.parse("2026-01-01T12:00:00Z");
        LeaderboardRow row = new LeaderboardRow(
                1,
                2L,
                "Ada Lovelace",
                3L,
                "Science Quiz",
                4L,
                "Science",
                5,
                4,
                80,
                submittedAt);
        when(leaderboardQueryRepository.findTopOverall(10)).thenReturn(List.of(row));

        LeaderboardViewResponse response = service.getLeaderboard(new LeaderboardFilterRequest());

        assertThat(response.entries()).hasSize(1);
        LeaderboardEntryResponse entry = response.entries().getFirst();
        assertThat(entry.rankPosition()).isEqualTo(1);
        assertThat(entry.userId()).isEqualTo(2L);
        assertThat(entry.userFullName()).isEqualTo("Ada Lovelace");
        assertThat(entry.quizId()).isEqualTo(3L);
        assertThat(entry.quizTitle()).isEqualTo("Science Quiz");
        assertThat(entry.categoryId()).isEqualTo(4L);
        assertThat(entry.categoryName()).isEqualTo("Science");
        assertThat(entry.totalQuestions()).isEqualTo(5);
        assertThat(entry.correctCount()).isEqualTo(4);
        assertThat(entry.scorePercentage()).isEqualTo(80);
        assertThat(entry.submittedAt()).isEqualTo(submittedAt);
    }
}
