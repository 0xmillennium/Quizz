package com.quizz.attempt.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.quizz.attempt.dto.QuizAttemptPageResponse;
import com.quizz.attempt.dto.QuizHistoryResponse;
import com.quizz.attempt.dto.QuizResultResponse;
import com.quizz.attempt.dto.ResultChartResponse;
import com.quizz.attempt.dto.StartQuizResponse;
import com.quizz.attempt.dto.SubmitQuizRequest;
import com.quizz.attempt.entity.QuizAttempt;
import com.quizz.attempt.mapper.QuizAttemptMapper;
import com.quizz.attempt.service.QuizAttemptCommandService;
import com.quizz.attempt.service.QuizAttemptQueryService;
import com.quizz.common.exception.NotFoundException;
import com.quizz.security.config.SecurityConfig;
import com.quizz.security.context.CurrentUserProvider;
import com.quizz.security.handler.CustomAuthenticationSuccessHandler;
import com.quizz.security.service.CustomUserDetailsService;
import com.quizz.user.entity.User;
import com.quizz.user.service.UserQueryService;
import jakarta.servlet.Filter;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class QuizAttemptControllerTest {

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private QuizAttemptCommandService quizAttemptCommandService;

    @Mock
    private QuizAttemptQueryService quizAttemptQueryService;

    @Mock
    private QuizAttemptMapper quizAttemptMapper;

    @Mock
    private QuizAttempt attempt;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        QuizAttemptController controller = new QuizAttemptController(
                currentUserProvider,
                quizAttemptCommandService,
                quizAttemptQueryService,
                quizAttemptMapper
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .apply(springSecurity(springSecurityFilterChain()))
                .build();
    }

    @Test
    void postStartRedirectsToAttempt() throws Exception {
        when(currentUserProvider.getCurrentUserId()).thenReturn(7L);
        when(quizAttemptCommandService.startAttempt(3L, 7L)).thenReturn(new StartQuizResponse(11L));

        mockMvc.perform(post("/attempts/start")
                        .param("quizId", "3")
                        .with(user("user@example.com").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/attempts/11"));

        verify(currentUserProvider).getCurrentUserId();
    }

    @Test
    void getAttemptReturnsPlayView() throws Exception {
        QuizAttemptPageResponse page = new QuizAttemptPageResponse(
                11L,
                "Science Quiz",
                "Science",
                30,
                Instant.parse("2026-01-01T12:00:00Z"),
                Instant.parse("2026-01-01T12:30:00Z"),
                List.of()
        );
        SubmitQuizRequest submitQuizRequest = new SubmitQuizRequest();
        when(currentUserProvider.getCurrentUserId()).thenReturn(7L);
        when(quizAttemptQueryService.getAttemptPage(11L, 7L)).thenReturn(attempt);
        when(quizAttemptMapper.toAttemptPageResponse(attempt)).thenReturn(page);
        when(quizAttemptMapper.toSubmitQuizRequest(attempt)).thenReturn(submitQuizRequest);

        mockMvc.perform(get("/attempts/11").with(user("user@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("attempt/play"))
                .andExpect(model().attribute("attempt", page))
                .andExpect(model().attribute("submitQuizRequest", submitQuizRequest));
    }

    @Test
    void postSubmitRedirectsToResult() throws Exception {
        when(currentUserProvider.getCurrentUserId()).thenReturn(7L);

        mockMvc.perform(post("/attempts/11/submit")
                        .with(user("user@example.com").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/attempts/11/result"));

        verify(quizAttemptCommandService).submitAttempt(org.mockito.ArgumentMatchers.eq(11L), org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.any(SubmitQuizRequest.class));
    }

    @Test
    void getResultReturnsResultView() throws Exception {
        QuizResultResponse result = resultResponse();
        when(currentUserProvider.getCurrentUserId()).thenReturn(7L);
        when(quizAttemptQueryService.getResult(11L, 7L)).thenReturn(attempt);
        when(quizAttemptMapper.toResultResponse(attempt)).thenReturn(result);

        mockMvc.perform(get("/attempts/11/result").with(user("user@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("attempt/result"))
                .andExpect(model().attribute("result", result));
    }

    @Test
    void getHistoryReturnsHistoryView() throws Exception {
        QuizHistoryResponse history = new QuizHistoryResponse(
                11L,
                "Science Quiz",
                "Science",
                "COMPLETED",
                2,
                1,
                50,
                Instant.parse("2026-01-01T12:00:00Z"),
                Instant.parse("2026-01-01T12:01:00Z")
        );
        when(currentUserProvider.getCurrentUserId()).thenReturn(7L);
        when(quizAttemptQueryService.findHistoryByUser(7L)).thenReturn(List.of(attempt));
        when(quizAttemptMapper.toHistoryResponseList(List.of(attempt))).thenReturn(List.of(history));

        mockMvc.perform(get("/attempts/history").with(user("user@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("attempt/history"))
                .andExpect(model().attribute("history", List.of(history)));
    }

    @Test
    void getChartDataReturnsJson() throws Exception {
        when(currentUserProvider.getCurrentUserId()).thenReturn(7L);
        when(quizAttemptQueryService.getResultChart(11L, 7L)).thenReturn(new ResultChartResponse(1, 2, 3));

        mockMvc.perform(get("/attempts/11/chart-data").with(user("user@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correctCount").value(1))
                .andExpect(jsonPath("$.wrongCount").value(2))
                .andExpect(jsonPath("$.unansweredCount").value(3));
    }

    @Test
    void anonymousAccessRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/attempts/history"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void postRoutesRequireCsrf() throws Exception {
        mockMvc.perform(post("/attempts/start")
                        .param("quizId", "3")
                        .with(user("user@example.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    private QuizResultResponse resultResponse() {
        return new QuizResultResponse(
                11L,
                "Science Quiz",
                "Science",
                "COMPLETED",
                2,
                1,
                1,
                0,
                50,
                "DEFAULT_V1",
                Instant.parse("2026-01-01T12:00:00Z"),
                Instant.parse("2026-01-01T12:30:00Z"),
                Instant.parse("2026-01-01T12:01:00Z"),
                List.of()
        );
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
