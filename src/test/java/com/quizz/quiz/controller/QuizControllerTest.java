package com.quizz.quiz.controller;

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
import com.quizz.quiz.dto.QuizDetailResponse;
import com.quizz.quiz.dto.QuizSummaryResponse;
import com.quizz.quiz.entity.Quiz;
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
class QuizControllerTest {

    @Mock
    private QuizQueryService quizQueryService;

    @Mock
    private QuizMapper quizMapper;

    @Mock
    private CategoryQueryService categoryQueryService;

    @Mock
    private CategoryMapper categoryMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        QuizController controller = new QuizController(
                quizQueryService,
                quizMapper,
                categoryQueryService,
                categoryMapper
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .apply(springSecurity(springSecurityFilterChain()))
                .build();
    }

    @Test
    void getQuizzesAsAuthenticatedUserReturnsListView() throws Exception {
        stubCategories();
        when(quizQueryService.findPublished()).thenReturn(List.of());
        when(quizMapper.toSummaryResponseList(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/quizzes").with(user("user@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("quiz/list"))
                .andExpect(model().attributeExists("quizzes"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    void getQuizzesAnonymousRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/quizzes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void getQuizzesWithCategoryIdReturnsListView() throws Exception {
        stubCategories();
        when(quizQueryService.findPublishedByCategory(1L)).thenReturn(List.of());
        when(quizMapper.toSummaryResponseList(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/quizzes")
                        .param("categoryId", "1")
                        .with(user("user@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("quiz/list"))
                .andExpect(model().attribute("selectedCategoryId", 1L));
    }

    @Test
    void getQuizDetailAsAuthenticatedUserReturnsDetailView() throws Exception {
        Quiz quiz = org.mockito.Mockito.mock(Quiz.class);
        QuizDetailResponse response = new QuizDetailResponse(1L, "Science Quiz", null, "Science", 30, 0, List.of());
        when(quizQueryService.getPublishedById(1L)).thenReturn(quiz);
        when(quizMapper.toDetailResponse(quiz)).thenReturn(response);

        mockMvc.perform(get("/quizzes/1").with(user("user@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("quiz/detail"))
                .andExpect(model().attribute("quiz", response));
    }

    @Test
    void getQuizDetailAnonymousRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/quizzes/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    private void stubCategories() {
        List<CategoryOptionResponse> categories = List.of(new CategoryOptionResponse(1L, "Science"));
        when(categoryQueryService.findActive()).thenReturn(List.of());
        when(categoryMapper.toOptionResponseList(List.of())).thenReturn(categories);
    }

    @SuppressWarnings("unused")
    private QuizSummaryResponse summaryResponse() {
        return new QuizSummaryResponse(1L, "Science Quiz", "Science", 30, 5);
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
