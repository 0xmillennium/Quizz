package com.quizz.auth.controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.quizz.auth.dto.RegisterRequest;
import com.quizz.auth.service.RegistrationService;
import com.quizz.auth.validation.RegistrationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class RegistrationControllerTest {

    @Mock
    private RegistrationService registrationService;

    @Mock
    private RegistrationValidator registrationValidator;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new RegistrationController(registrationService, registrationValidator))
                .setValidator(validator)
                .build();
    }

    @Test
    void getRegisterReturnsRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void postValidRegisterRedirectsToLogin() throws Exception {
        mockMvc.perform(post("/register")
                        .param("fullName", "Ada Lovelace")
                        .param("email", "ada@example.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(registrationService).register(org.mockito.ArgumentMatchers.any(RegisterRequest.class));
    }

    @Test
    void postInvalidRegisterReturnsRegisterView() throws Exception {
        mockMvc.perform(post("/register")
                        .param("fullName", "")
                        .param("email", "not-an-email")
                        .param("password", "short")
                        .param("confirmPassword", "short"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));

        verify(registrationService, never()).register(org.mockito.ArgumentMatchers.any(RegisterRequest.class));
    }
}
