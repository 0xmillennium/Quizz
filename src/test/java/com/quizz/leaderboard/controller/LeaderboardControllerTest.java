package com.quizz.leaderboard.controller;

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

import com.quizz.category.dto.CategoryOptionResponse;
import com.quizz.category.mapper.CategoryMapper;
import com.quizz.category.service.CategoryQueryService;
import com.quizz.common.exception.NotFoundException;
import com.quizz.leaderboard.dto.LeaderboardFilterRequest;
import com.quizz.leaderboard.dto.LeaderboardViewResponse;
import com.quizz.leaderboard.service.LeaderboardService;
import com.quizz.quiz.dto.QuizSummaryResponse;
import com.quizz.quiz.mapper.QuizMapper;
import com.quizz.quiz.service.QuizQueryService;
import com.quizz.security.config.SecurityConfig;
import com.quizz.security.handler.CustomAuthenticationSuccessHandler;
import com.quizz.security.service.CustomUserDetailsService;
import com.quizz.user.entity.User;
import com.quizz.user.service.UserQueryService;
import jakarta.servlet.Filter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class LeaderboardControllerTest {

    @Mock
    private LeaderboardService leaderboardService;

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
        LeaderboardController controller = new LeaderboardController(
                leaderboardService,
                categoryQueryService,
                categoryMapper,
                quizQueryService,
                quizMapper
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .apply(springSecurity(springSecurityFilterChain()))
                .build();
    }

    @Test
    void getLeaderboardAsAuthenticatedUserReturnsIndexView() throws Exception {
        LeaderboardViewResponse leaderboard = stubPageDependencies();

        mockMvc.perform(get("/leaderboard").with(user("user@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("leaderboard/index"))
                .andExpect(model().attribute("leaderboard", leaderboard))
                .andExpect(model().attributeExists("filter"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("quizzes"));
    }

    @Test
    void getLeaderboardAsAdminReturnsIndexView() throws Exception {
        stubPageDependencies();

        mockMvc.perform(get("/leaderboard").with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("leaderboard/index"));
    }

    @Test
    void getLeaderboardAnonymousRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/leaderboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void getLeaderboardWithQuizIdPassesFilterToService() throws Exception {
        stubPageDependencies();

        mockMvc.perform(get("/leaderboard")
                        .param("quizId", "7")
                        .with(user("user@example.com").roles("USER")))
                .andExpect(status().isOk());

        LeaderboardFilterRequest filter = captureFilter();
        assertThat(filter.getQuizId()).isEqualTo(7L);
    }

    @Test
    void getLeaderboardWithCategoryIdPassesFilterToService() throws Exception {
        stubPageDependencies();

        mockMvc.perform(get("/leaderboard")
                        .param("categoryId", "4")
                        .with(user("user@example.com").roles("USER")))
                .andExpect(status().isOk());

        LeaderboardFilterRequest filter = captureFilter();
        assertThat(filter.getCategoryId()).isEqualTo(4L);
    }

    @Test
    void getLeaderboardWithLimitPassesFilterToService() throws Exception {
        stubPageDependencies();

        mockMvc.perform(get("/leaderboard")
                        .param("limit", "25")
                        .with(user("user@example.com").roles("USER")))
                .andExpect(status().isOk());

        LeaderboardFilterRequest filter = captureFilter();
        assertThat(filter.getLimit()).isEqualTo(25);
    }

    @Test
    void getLeaderboardAddsCategoriesAndQuizzesToModel() throws Exception {
        List<CategoryOptionResponse> categories = List.of(new CategoryOptionResponse(1L, "Science"));
        List<QuizSummaryResponse> quizzes = List.of(new QuizSummaryResponse(2L, "Science Quiz", "Science", 30, 5));
        when(leaderboardService.getLeaderboard(any(LeaderboardFilterRequest.class))).thenReturn(emptyLeaderboard());
        when(categoryQueryService.findActive()).thenReturn(List.of());
        when(categoryMapper.toOptionResponseList(List.of())).thenReturn(categories);
        when(quizQueryService.findPublished()).thenReturn(List.of());
        when(quizMapper.toSummaryResponseList(List.of())).thenReturn(quizzes);

        mockMvc.perform(get("/leaderboard").with(user("user@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("categories", categories))
                .andExpect(model().attribute("quizzes", quizzes));
    }

    private LeaderboardViewResponse stubPageDependencies() {
        LeaderboardViewResponse leaderboard = emptyLeaderboard();
        when(leaderboardService.getLeaderboard(any(LeaderboardFilterRequest.class))).thenReturn(leaderboard);
        when(categoryQueryService.findActive()).thenReturn(List.of());
        when(categoryMapper.toOptionResponseList(List.of())).thenReturn(List.of());
        when(quizQueryService.findPublished()).thenReturn(List.of());
        when(quizMapper.toSummaryResponseList(List.of())).thenReturn(List.of());
        return leaderboard;
    }

    private LeaderboardViewResponse emptyLeaderboard() {
        return new LeaderboardViewResponse("OVERALL", null, null, 10, List.of());
    }

    private LeaderboardFilterRequest captureFilter() {
        ArgumentCaptor<LeaderboardFilterRequest> captor = ArgumentCaptor.forClass(LeaderboardFilterRequest.class);
        verify(leaderboardService).getLeaderboard(captor.capture());
        return captor.getValue();
    }

    private Filter springSecurityFilterChain() throws Exception {
        CustomUserDetailsService userDetailsService = new CustomUserDetailsService(new MissingUserQueryService());
        SecurityConfig securityConfig = new SecurityConfig(userDetailsService, new CustomAuthenticationSuccessHandler());
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        AuthenticationProvider authenticationProvider = securityConfig.authenticationProvider(passwordEncoder);
        SecurityFilterChain securityFilterChain = securityConfig.securityFilterChain(
                httpSecurity(authenticationProvider),
                authenticationProvider
        );
        return new FilterChainProxy(securityFilterChain);
    }

    private HttpSecurity httpSecurity(AuthenticationProvider authenticationProvider) {
        ObjectPostProcessor<Object> objectPostProcessor = new PassthroughObjectPostProcessor();
        AuthenticationManagerBuilder authenticationManagerBuilder = new AuthenticationManagerBuilder(objectPostProcessor);
        authenticationManagerBuilder.authenticationProvider(authenticationProvider);

        ApplicationContext applicationContext = new StaticApplicationContext();
        Map<Class<?>, Object> sharedObjects = new HashMap<>();
        sharedObjects.put(ApplicationContext.class, applicationContext);
        sharedObjects.put(PathPatternRequestMatcher.Builder.class, PathPatternRequestMatcher.withDefaults());

        HttpSecurity httpSecurity = new HttpSecurity(objectPostProcessor, authenticationManagerBuilder, sharedObjects);
        httpSecurity.setSharedObject(ApplicationContext.class, applicationContext);
        httpSecurity.setSharedObject(PathPatternRequestMatcher.Builder.class, PathPatternRequestMatcher.withDefaults());
        return httpSecurity;
    }

    private static class PassthroughObjectPostProcessor implements ObjectPostProcessor<Object> {

        @Override
        public <O> O postProcess(O object) {
            return object;
        }
    }

    private static class MissingUserQueryService implements UserQueryService {

        @Override
        public boolean existsByEmail(String email) {
            return false;
        }

        @Override
        public User getByEmail(String email) {
            throw new NotFoundException("missing");
        }

        @Override
        public User getById(Long id) {
            throw new NotFoundException("missing");
        }
    }
}
