package com.onlinequiz.auth.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.onlinequiz.auth.dto.RegisterRequest;
import com.onlinequiz.user.service.UserQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

@ExtendWith(MockitoExtension.class)
class RegistrationValidatorTest {

    @Mock
    private UserQueryService userQueryService;

    private RegistrationValidator validator;

    @BeforeEach
    void setUp() {
        validator = new RegistrationValidator(userQueryService);
    }

    @Test
    void matchingPasswordsAddNoConfirmPasswordError() {
        RegisterRequest request = request("password123", "password123");
        BindingResult bindingResult = bindingResult(request);

        validator.validate(request, bindingResult);

        assertThat(bindingResult.getFieldError("confirmPassword")).isNull();
    }

    @Test
    void mismatchedPasswordsAddsConfirmPasswordError() {
        RegisterRequest request = request("password123", "different123");
        BindingResult bindingResult = bindingResult(request);

        validator.validate(request, bindingResult);

        assertThat(bindingResult.getFieldError("confirmPassword")).isNotNull();
    }

    @Test
    void existingEmailAddsEmailError() {
        RegisterRequest request = request("password123", "password123");
        when(userQueryService.existsByEmail("ada@example.com")).thenReturn(true);
        BindingResult bindingResult = bindingResult(request);

        validator.validate(request, bindingResult);

        assertThat(bindingResult.getFieldError("email")).isNotNull();
    }

    private RegisterRequest request(String password, String confirmPassword) {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Ada Lovelace");
        request.setEmail("ada@example.com");
        request.setPassword(password);
        request.setConfirmPassword(confirmPassword);
        return request;
    }

    private BindingResult bindingResult(RegisterRequest request) {
        return new BeanPropertyBindingResult(request, "registerRequest");
    }
}
