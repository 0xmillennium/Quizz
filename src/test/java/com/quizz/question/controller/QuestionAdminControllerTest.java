package com.quizz.question.controller;

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
import com.quizz.question.dto.QuestionCreateRequest;
import com.quizz.question.dto.QuestionSummaryResponse;
import com.quizz.question.dto.QuestionUpdateRequest;
import com.quizz.question.entity.Question;
import com.quizz.question.mapper.QuestionMapper;
import com.quizz.question.service.QuestionCommandService;
import com.quizz.question.service.QuestionQueryService;
import com.quizz.question.validation.QuestionFormValidator;
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
class QuestionAdminControllerTest {

    @Mock
    private QuestionCommandService questionCommandService;

    @Mock
    private QuestionQueryService questionQueryService;

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private QuestionFormValidator questionFormValidator;

    @Mock
    private CategoryQueryService categoryQueryService;

    @Mock
    private CategoryMapper categoryMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        QuestionAdminController controller = new QuestionAdminController(
                questionCommandService,
                questionQueryService,
                questionMapper,
                questionFormValidator,
                categoryQueryService,
                categoryMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setValidator(validator)
                .apply(springSecurity(springSecurityFilterChain()))
                .build();
    }

    @Test
    void getQuestionsAsAdminReturnsListView() throws Exception {
        List<Question> questions = List.of();
        List<QuestionSummaryResponse> responses = List.of();
        when(questionQueryService.findAllForAdmin()).thenReturn(questions);
        when(questionMapper.toSummaryResponseList(questions)).thenReturn(responses);

        mockMvc.perform(get("/admin/questions").with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/questions/list"))
                .andExpect(model().attributeExists("questions"));
    }

    @Test
    void getQuestionsAsUserIsForbidden() throws Exception {
        mockMvc.perform(get("/admin/questions").with(user("user@example.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getNewQuestionAsAdminReturnsCreateView() throws Exception {
        stubActiveCategories();

        mockMvc.perform(get("/admin/questions/new").with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/questions/create"))
                .andExpect(model().attributeExists("questionCreateRequest"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    void postQuestionsValidRedirectsToQuestions() throws Exception {
        mockMvc.perform(post("/admin/questions")
                .with(user("admin@example.com").roles("ADMIN"))
                .with(csrf())
                .param("text", "What is water?")
                .param("categoryId", "1")
                .param("options[0].text", "H2O")
                .param("options[0].correct", "true")
                .param("options[1].text", "CO2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/questions"));

        verify(questionCommandService).create(any(QuestionCreateRequest.class));
    }

    @Test
    void postQuestionsInvalidReturnsCreateView() throws Exception {
        stubActiveCategories();

        mockMvc.perform(post("/admin/questions")
                .with(user("admin@example.com").roles("ADMIN"))
                .with(csrf())
                .param("text", "")
                .param("categoryId", "1")
                .param("options[0].text", "H2O")
                .param("options[0].correct", "true")
                .param("options[1].text", "CO2"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/questions/create"));

        verify(questionCommandService, never()).create(any(QuestionCreateRequest.class));
    }

    @Test
    void getEditQuestionReturnsEditView() throws Exception {
        Question question = org.mockito.Mockito.mock(Question.class);
        QuestionUpdateRequest request = new QuestionUpdateRequest();
        when(questionQueryService.getByIdWithDetails(1L)).thenReturn(question);
        when(questionMapper.toUpdateRequest(question)).thenReturn(request);
        stubActiveCategories();

        mockMvc.perform(get("/admin/questions/1/edit").with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/questions/edit"))
                .andExpect(model().attribute("questionId", 1L))
                .andExpect(model().attributeExists("questionUpdateRequest"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    void postQuestionUpdateValidRedirectsToQuestions() throws Exception {
        mockMvc.perform(post("/admin/questions/1")
                .with(user("admin@example.com").roles("ADMIN"))
                .with(csrf())
                .param("text", "What is water?")
                .param("categoryId", "1")
                .param("options[0].text", "H2O")
                .param("options[0].correct", "true")
                .param("options[1].text", "CO2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/questions"));

        verify(questionCommandService).update(org.mockito.ArgumentMatchers.eq(1L), any(QuestionUpdateRequest.class));
    }

    @Test
    void postQuestionUpdateInvalidReturnsEditView() throws Exception {
        stubActiveCategories();

        mockMvc.perform(post("/admin/questions/1")
                .with(user("admin@example.com").roles("ADMIN"))
                .with(csrf())
                .param("text", "")
                .param("categoryId", "1")
                .param("options[0].text", "H2O")
                .param("options[0].correct", "true")
                .param("options[1].text", "CO2"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/questions/edit"))
                .andExpect(model().attribute("questionId", 1L));

        verify(questionCommandService, never()).update(
                org.mockito.ArgumentMatchers.any(),
                any(QuestionUpdateRequest.class));
    }

    @Test
    void postQuestionDeleteRedirectsToQuestions() throws Exception {
        mockMvc.perform(post("/admin/questions/1/delete")
                .with(user("admin@example.com").roles("ADMIN"))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/questions"));

        verify(questionCommandService).archive(1L);
    }

    @Test
    void postQuestionRestoreRedirectsToQuestions() throws Exception {
        mockMvc.perform(post("/admin/questions/1/restore")
                .with(user("admin@example.com").roles("ADMIN"))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/questions"));

        verify(questionCommandService).restore(1L);
    }

    private void stubActiveCategories() {
        List<CategoryOptionResponse> responses = List.of(new CategoryOptionResponse(1L, "Science"));
        when(categoryQueryService.findActive()).thenReturn(List.of());
        when(categoryMapper.toOptionResponseList(List.of())).thenReturn(responses);
    }

    private Filter springSecurityFilterChain() throws Exception {
        CustomUserDetailsService userDetailsService = new CustomUserDetailsService(new MissingUserQueryService());
        SecurityConfig securityConfig = new SecurityConfig(userDetailsService,
                new CustomAuthenticationSuccessHandler());
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        AuthenticationProvider authenticationProvider = securityConfig.authenticationProvider(passwordEncoder);
        SecurityFilterChain securityFilterChain = securityConfig.securityFilterChain(
                httpSecurity(authenticationProvider),
                authenticationProvider);
        return new FilterChainProxy(securityFilterChain);
    }

    private HttpSecurity httpSecurity(AuthenticationProvider authenticationProvider) {
        ObjectPostProcessor<Object> objectPostProcessor = new PassthroughObjectPostProcessor();
        AuthenticationManagerBuilder authenticationManagerBuilder = new AuthenticationManagerBuilder(
                objectPostProcessor);
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
