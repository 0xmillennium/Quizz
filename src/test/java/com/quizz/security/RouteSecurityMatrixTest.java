package com.quizz.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quizz.common.exception.NotFoundException;
import com.quizz.security.config.SecurityConfig;
import com.quizz.security.handler.CustomAuthenticationSuccessHandler;
import com.quizz.security.service.CustomUserDetailsService;
import com.quizz.user.entity.User;
import com.quizz.user.service.UserQueryService;
import jakarta.servlet.Filter;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

class RouteSecurityMatrixTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new MatrixRoutesController())
                .apply(springSecurity(springSecurityFilterChain()))
                .build();
    }

    @Test
    void anonymousRoutesFollowMatrix() throws Exception {
        expectAnonymousAllowed("/login");
        expectAnonymousAllowed("/register");
        expectAnonymousRedirectsToLogin("/quizzes");
        expectAnonymousRedirectsToLogin("/leaderboard");
        expectAnonymousRedirectsToLogin("/attempts/history");
        expectAnonymousRedirectsToLogin("/admin");
        expectAnonymousRedirectsToLogin("/admin/results");
    }

    @Test
    void userRoutesFollowMatrix() throws Exception {
        expectUserAllowed("/quizzes");
        expectUserAllowed("/leaderboard");
        expectUserAllowed("/attempts/history");
        expectUserForbidden("/admin");
        expectUserForbidden("/admin/categories");
        expectUserForbidden("/admin/questions");
        expectUserForbidden("/admin/quizzes");
        expectUserForbidden("/admin/results");
    }

    @Test
    void adminRoutesFollowMatrix() throws Exception {
        expectAdminAllowed("/admin");
        expectAdminAllowed("/admin/dashboard");
        expectAdminAllowed("/admin/categories");
        expectAdminAllowed("/admin/questions");
        expectAdminAllowed("/admin/quizzes");
        expectAdminAllowed("/admin/results");
        expectAdminAllowed("/leaderboard");
    }

    private void expectAnonymousAllowed(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isOk());
    }

    private void expectAnonymousRedirectsToLogin(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    private void expectUserAllowed(String path) throws Exception {
        mockMvc.perform(get(path).with(user("user@example.com").roles("USER")))
                .andExpect(status().isOk());
    }

    private void expectUserForbidden(String path) throws Exception {
        mockMvc.perform(get(path).with(user("user@example.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    private void expectAdminAllowed(String path) throws Exception {
        mockMvc.perform(get(path).with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk());
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

    @Controller
    static class MatrixRoutesController {

        @GetMapping({"/login", "/register"})
        @ResponseBody
        String publicPage() {
            return "public";
        }

        @GetMapping({"/quizzes", "/leaderboard", "/attempts/history"})
        @ResponseBody
        String userPage() {
            return "user";
        }

        @GetMapping({
                "/admin",
                "/admin/dashboard",
                "/admin/categories",
                "/admin/questions",
                "/admin/quizzes",
                "/admin/results"
        })
        @ResponseBody
        String adminPage() {
            return "admin";
        }
    }
}
