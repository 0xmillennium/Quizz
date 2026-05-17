package com.onlinequiz.auth.validation;

import com.onlinequiz.auth.dto.RegisterRequest;
import com.onlinequiz.user.service.UserQueryService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;

@Component
public class RegistrationValidator {

    private final UserQueryService userQueryService;

    public RegistrationValidator(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    public void validate(RegisterRequest request, BindingResult bindingResult) {
        if (request.getPassword() != null
                && request.getConfirmPassword() != null
                && !request.getPassword().equals(request.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match.");
        }

        if (StringUtils.hasText(request.getEmail()) && userQueryService.existsByEmail(request.getEmail())) {
            bindingResult.rejectValue("email", "email.duplicate", "Email is already registered.");
        }
    }
}
