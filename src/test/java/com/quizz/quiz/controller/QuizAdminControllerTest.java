package com.quizz.quiz.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.quizz.category.dto.CategoryOptionResponse;
import com.quizz.category.mapper.CategoryMapper;
import com.quizz.category.service.CategoryQueryService;
import com.quizz.common.exception.NotFoundException;
import com.quizz.question.dto.QuestionSelectionResponse;
import com.quizz.question.mapper.QuestionMapper;
import com.quizz.question.service.QuestionQueryService;
import com.quizz.quiz.dto.QuizAdminResponse;
import com.quizz.quiz.dto.QuizCreateRequest;
import com.quizz.quiz.dto.QuizUpdateRequest;
import com.quizz.quiz.entity.Quiz;
import com.quizz.quiz.mapper.QuizMapper;
import com.quizz.quiz.service.QuizCommandService;
import com.quizz.quiz.service.QuizQueryService;
import com.quizz.quiz.validation.QuizFormValidator;
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
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class QuizAdminControllerTest {

    @Mock
    private QuizCommandService quizCommandService;

    @Mock
    private QuizQueryService quizQueryService;

    @Mock
    private QuizMapper quizMapper;

    @Mock
    private QuizFormValidator quizFormValidator;

    @Mock
    private CategoryQueryService categoryQueryService;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private QuestionQueryService questionQueryService;

    @Mock
    private QuestionMapper questionMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        QuizAdminController controller = new QuizAdminController(
                quizCommandService,
                quizQueryService,
                quizMapper,
                quizFormValidator,
                categoryQueryService,
                categoryMapper,
                questionQueryService,
                questionMapper
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setValidator(validator)
                .apply(springSecurity(springSecurityFilterChain()))
                .build();
    }

    @Test
    void getAdminQuizzesAsAdminReturnsListView() throws Exception {
        when(quizQueryService.findAllForAdmin()).thenReturn(List.of());
        when(quizMapper.toAdminResponseList(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/admin/quizzes").with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/quizzes/list"))
                .andExpect(model().attributeExists("quizzes"));
    }

    @Test
    void getAdminQuizzesAsUserIsForbidden() throws Exception {
        mockMvc.perform(get("/admin/quizzes").with(user("user@example.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getNewQuizAsAdminReturnsCreateView() throws Exception {
        stubFormOptions();

        mockMvc.perform(get("/admin/quizzes/new").with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/quizzes/create"))
                .andExpect(model().attributeExists("quizCreateRequest"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("questions"));
    }

    @Test
    void postAdminQuizzesValidRedirectsToQuizzes() throws Exception {
        mockMvc.perform(post("/admin/quizzes")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .with(csrf())
                        .param("title", "Science Quiz")
                        .param("categoryId", "1")
                        .param("durationMinutes", "30")
                        .param("questionCount", "1")
                        .param("attemptLimit", "3")
                        .param("retakeCooldownMinutes", "1440")
                        .param("questionIds", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/quizzes"));

        verify(quizCommandService).create(any(QuizCreateRequest.class));
    }

    @Test
    void postAdminQuizzesInvalidReturnsCreateView() throws Exception {
        stubFormOptions();

        mockMvc.perform(post("/admin/quizzes")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .with(csrf())
                        .param("title", "")
                        .param("categoryId", "1")
                        .param("durationMinutes", "30")
                        .param("questionCount", "1")
                        .param("attemptLimit", "3")
                        .param("retakeCooldownMinutes", "1440")
                        .param("questionIds", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/quizzes/create"));

        verify(quizCommandService, never()).create(any(QuizCreateRequest.class));
    }

    @Test
    void getAdminQuizDetailReturnsDetailView() throws Exception {
        Quiz quiz = org.mockito.Mockito.mock(Quiz.class);
        QuizAdminResponse response = adminResponse("DRAFT");
        when(quizQueryService.getByIdWithAdminDetails(1L)).thenReturn(quiz);
        when(quizMapper.toAdminResponse(quiz)).thenReturn(response);

        mockMvc.perform(get("/admin/quizzes/1").with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/quizzes/detail"))
                .andExpect(model().attribute("quiz", response));
    }

    @Test
    void getEditDraftQuizReturnsEditView() throws Exception {
        Quiz quiz = org.mockito.Mockito.mock(Quiz.class);
        QuizUpdateRequest request = updateRequest();
        when(quiz.isDraft()).thenReturn(true);
        when(quizQueryService.getByIdWithAdminDetails(1L)).thenReturn(quiz);
        when(quizMapper.toUpdateRequest(quiz)).thenReturn(request);
        stubFormOptions();

        mockMvc.perform(get("/admin/quizzes/1/edit").with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/quizzes/edit"))
                .andExpect(model().attribute("quizId", 1L))
                .andExpect(model().attribute("quizUpdateRequest", request));
    }

    @Test
    void postAdminQuizUpdateValidRedirectsToQuizDetail() throws Exception {
        mockMvc.perform(post("/admin/quizzes/1")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .with(csrf())
                        .param("title", "Science Quiz")
                        .param("categoryId", "1")
                        .param("durationMinutes", "30")
                        .param("questionCount", "1")
                        .param("attemptLimit", "3")
                        .param("retakeCooldownMinutes", "1440")
                        .param("questionIds", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/quizzes/1"));

        verify(quizCommandService).updateDraft(org.mockito.ArgumentMatchers.eq(1L), any(QuizUpdateRequest.class));
    }

    @Test
    void postAdminQuizUpdateInvalidReturnsEditView() throws Exception {
        stubFormOptions();

        mockMvc.perform(post("/admin/quizzes/1")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .with(csrf())
                        .param("title", "")
                        .param("categoryId", "1")
                        .param("durationMinutes", "30")
                        .param("questionCount", "1")
                        .param("attemptLimit", "3")
                        .param("retakeCooldownMinutes", "1440")
                        .param("questionIds", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/quizzes/edit"))
                .andExpect(model().attribute("quizId", 1L));

        verify(quizCommandService, never()).updateDraft(
                org.mockito.ArgumentMatchers.any(),
                any(QuizUpdateRequest.class)
        );
    }

    @Test
    void postPublishRedirectsToQuizDetail() throws Exception {
        mockMvc.perform(post("/admin/quizzes/1/publish")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/quizzes/1"));

        verify(quizCommandService).publish(1L);
    }

    @Test
    void postArchiveRedirectsToQuizzes() throws Exception {
        mockMvc.perform(post("/admin/quizzes/1/archive")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/quizzes"));

        verify(quizCommandService).archive(1L);
    }

    private void stubFormOptions() {
        List<CategoryOptionResponse> categories = List.of(new CategoryOptionResponse(1L, "Science"));
        List<QuestionSelectionResponse> questions = List.of(
                new QuestionSelectionResponse(10L, "Question?", 1L, "Science")
        );
        when(categoryQueryService.findActive()).thenReturn(List.of());
        when(categoryMapper.toOptionResponseList(List.of())).thenReturn(categories);
        when(questionQueryService.findActive()).thenReturn(List.of());
        when(questionMapper.toSelectionResponseList(List.of())).thenReturn(questions);
    }

    private QuizAdminResponse adminResponse(String status) {
        return new QuizAdminResponse(1L, "Science Quiz", null, 1L, "Science", 30, 1, 3, 1440, status, 0, List.of());
    }

    private QuizUpdateRequest updateRequest() {
        QuizUpdateRequest request = new QuizUpdateRequest();
        request.setTitle("Science Quiz");
        request.setCategoryId(1L);
        request.setDurationMinutes(30);
        request.setQuestionIds(List.of(10L));
        return request;
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
