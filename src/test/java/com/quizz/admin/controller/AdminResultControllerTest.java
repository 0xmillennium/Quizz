package com.quizz.admin.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.quizz.admin.dto.AdminPageResponse;
import com.quizz.admin.dto.AdminResultDetailResponse;
import com.quizz.admin.dto.AdminResultFilterRequest;
import com.quizz.admin.dto.AdminResultListResponse;
import com.quizz.admin.service.AdminResultService;
import com.quizz.category.dto.CategoryOptionResponse;
import com.quizz.category.mapper.CategoryMapper;
import com.quizz.category.service.CategoryQueryService;
import com.quizz.quiz.dto.QuizSummaryResponse;
import com.quizz.quiz.mapper.QuizMapper;
import com.quizz.quiz.service.QuizQueryService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AdminResultControllerTest {

    @Mock
    private AdminResultService adminResultService;

    @Mock
    private CategoryQueryService categoryQueryService;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private QuizQueryService quizQueryService;

    @Mock
    private QuizMapper quizMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        AdminResultController controller = new AdminResultController(
                adminResultService,
                categoryQueryService,
                categoryMapper,
                quizQueryService,
                quizMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .apply(springSecurity(AdminControllerSecurityTestSupport.springSecurityFilterChain()))
                .build();
    }

    @Test
    void getResultsAsAdminReturnsListViewWithModelAttributes() throws Exception {
        AdminResultListResponse response = emptyResults(new AdminResultFilterRequest());
        List<CategoryOptionResponse> categories = List.of(new CategoryOptionResponse(1L, "Science"));
        List<QuizSummaryResponse> quizzes = List
                .of(new QuizSummaryResponse(2L, "Science Quiz", "Science", 30, 5, 3, 1440));
        when(adminResultService.searchResults(any(AdminResultFilterRequest.class))).thenReturn(response);
        when(categoryQueryService.findActive()).thenReturn(List.of());
        when(categoryMapper.toOptionResponseList(List.of())).thenReturn(categories);
        when(quizQueryService.findPublished()).thenReturn(List.of());
        when(quizMapper.toSummaryResponseList(List.of())).thenReturn(quizzes);

        mockMvc.perform(get("/admin/results").with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/results/list"))
                .andExpect(model().attribute("results", response))
                .andExpect(model().attribute("filter", response.filter()))
                .andExpect(model().attribute("categories", categories))
                .andExpect(model().attribute("quizzes", quizzes))
                .andExpect(model().attribute("statuses", List.of("IN_PROGRESS", "COMPLETED", "ABANDONED")));
    }

    @Test
    void getResultsAsUserIsForbidden() throws Exception {
        mockMvc.perform(get("/admin/results").with(user("user@example.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getResultsAnonymousRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/results"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void getResultsWithFiltersPassesFilterToService() throws Exception {
        AdminResultFilterRequest normalized = new AdminResultFilterRequest();
        normalized.setUserId(1L);
        normalized.setQuizId(2L);
        normalized.setCategoryId(3L);
        normalized.setStatus("COMPLETED");
        when(adminResultService.searchResults(any(AdminResultFilterRequest.class)))
                .thenReturn(emptyResults(normalized));
        when(categoryQueryService.findActive()).thenReturn(List.of());
        when(categoryMapper.toOptionResponseList(List.of())).thenReturn(List.of());
        when(quizQueryService.findPublished()).thenReturn(List.of());
        when(quizMapper.toSummaryResponseList(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/admin/results")
                .param("userId", "1")
                .param("quizId", "2")
                .param("categoryId", "3")
                .param("status", "completed")
                .param("page", "2")
                .param("size", "50")
                .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/results/list"));

        ArgumentCaptor<AdminResultFilterRequest> filterCaptor = ArgumentCaptor.forClass(AdminResultFilterRequest.class);
        verify(adminResultService).searchResults(filterCaptor.capture());
        AdminResultFilterRequest filter = filterCaptor.getValue();
        assertThat(filter.getUserId()).isEqualTo(1L);
        assertThat(filter.getQuizId()).isEqualTo(2L);
        assertThat(filter.getCategoryId()).isEqualTo(3L);
        assertThat(filter.getStatus()).isEqualTo("completed");
        assertThat(filter.getPage()).isEqualTo(2);
        assertThat(filter.getSize()).isEqualTo(50);
    }

    @Test
    void getResultDetailAsAdminReturnsDetailView() throws Exception {
        AdminResultDetailResponse detail = detail();
        when(adminResultService.getResultDetail(1L)).thenReturn(detail);

        mockMvc.perform(get("/admin/results/1").with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/results/detail"))
                .andExpect(model().attribute("result", detail));
    }

    @Test
    void getResultDetailAsUserIsForbidden() throws Exception {
        mockMvc.perform(get("/admin/results/1").with(user("user@example.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    private AdminResultListResponse emptyResults(AdminResultFilterRequest filter) {
        return new AdminResultListResponse(
                filter,
                new AdminPageResponse(1, 20, 0, 0, false, false),
                List.of());
    }

    private AdminResultDetailResponse detail() {
        return new AdminResultDetailResponse(
                1L,
                2L,
                "Ada Lovelace",
                "Science Quiz",
                3L,
                4L,
                "Science",
                "COMPLETED",
                "MANUAL",
                5,
                4,
                1,
                0,
                80,
                "v1",
                null,
                null,
                null,
                null,
                List.of());
    }
}
