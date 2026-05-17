package com.quizz.category.controller;

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

import com.quizz.category.dto.CategoryCreateRequest;
import com.quizz.category.dto.CategoryUpdateRequest;
import com.quizz.category.entity.Category;
import com.quizz.category.mapper.CategoryMapper;
import com.quizz.category.service.CategoryCommandService;
import com.quizz.category.service.CategoryQueryService;
import com.quizz.category.validation.CategoryFormValidator;
import com.quizz.common.exception.NotFoundException;
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
class CategoryAdminControllerTest {

    @Mock
    private CategoryCommandService categoryCommandService;

    @Mock
    private CategoryQueryService categoryQueryService;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private CategoryFormValidator categoryFormValidator;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        CategoryAdminController controller = new CategoryAdminController(
                categoryCommandService,
                categoryQueryService,
                categoryMapper,
                categoryFormValidator
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setValidator(validator)
                .apply(springSecurity(springSecurityFilterChain()))
                .build();
    }

    @Test
    void getCategoriesAsAdminReturnsListView() throws Exception {
        when(categoryQueryService.findAll()).thenReturn(List.of());
        when(categoryMapper.toResponseList(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/admin/categories").with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categories/list"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    void getCategoriesAsUserIsForbidden() throws Exception {
        mockMvc.perform(get("/admin/categories").with(user("user@example.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getNewCategoryAsAdminReturnsCreateView() throws Exception {
        mockMvc.perform(get("/admin/categories/new").with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categories/create"))
                .andExpect(model().attributeExists("categoryCreateRequest"));
    }

    @Test
    void postCategoriesValidRedirectsToCategories() throws Exception {
        mockMvc.perform(post("/admin/categories")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .with(csrf())
                        .param("name", "Science")
                        .param("description", "Questions about science"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));

        verify(categoryCommandService).create(any(CategoryCreateRequest.class));
    }

    @Test
    void postCategoriesInvalidReturnsCreateView() throws Exception {
        mockMvc.perform(post("/admin/categories")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .with(csrf())
                        .param("name", "")
                        .param("description", "Questions about science"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categories/create"));

        verify(categoryCommandService, never()).create(any(CategoryCreateRequest.class));
    }

    @Test
    void getEditCategoryReturnsEditView() throws Exception {
        Category category = Category.create("Science", null);
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName("Science");
        when(categoryQueryService.getById(1L)).thenReturn(category);
        when(categoryMapper.toUpdateRequest(category)).thenReturn(request);

        mockMvc.perform(get("/admin/categories/1/edit").with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categories/edit"))
                .andExpect(model().attribute("categoryId", 1L))
                .andExpect(model().attributeExists("categoryUpdateRequest"));
    }

    @Test
    void postCategoryUpdateValidRedirectsToCategories() throws Exception {
        mockMvc.perform(post("/admin/categories/1")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .with(csrf())
                        .param("name", "Science")
                        .param("description", "Questions about science"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));

        verify(categoryCommandService).update(org.mockito.ArgumentMatchers.eq(1L), any(CategoryUpdateRequest.class));
    }

    @Test
    void postCategoryUpdateInvalidReturnsEditView() throws Exception {
        mockMvc.perform(post("/admin/categories/1")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .with(csrf())
                        .param("name", "")
                        .param("description", "Questions about science"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categories/edit"))
                .andExpect(model().attribute("categoryId", 1L));

        verify(categoryCommandService, never()).update(
                org.mockito.ArgumentMatchers.any(),
                any(CategoryUpdateRequest.class)
        );
    }

    @Test
    void postActivateCategoryRedirectsToCategories() throws Exception {
        mockMvc.perform(post("/admin/categories/1/activate")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));

        verify(categoryCommandService).activate(1L);
    }

    @Test
    void postDeactivateCategoryRedirectsToCategories() throws Exception {
        mockMvc.perform(post("/admin/categories/1/deactivate")
                        .with(user("admin@example.com").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));

        verify(categoryCommandService).deactivate(1L);
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
