package com.quizz.auth.service;

import com.quizz.auth.dto.RegisterRequest;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.user.service.CreateUserCommand;
import com.quizz.user.entity.UserRole;
import com.quizz.user.service.UserAccountService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultRegistrationService implements RegistrationService {

    private final PasswordEncoder passwordEncoder;
    private final UserAccountService userAccountService;

    public DefaultRegistrationService(PasswordEncoder passwordEncoder, UserAccountService userAccountService) {
        this.passwordEncoder = passwordEncoder;
        this.userAccountService = userAccountService;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessRuleException("Passwords do not match.");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        CreateUserCommand command = new CreateUserCommand(
                request.getFullName(),
                request.getEmail(),
                passwordHash,
                UserRole.USER);

        userAccountService.createUser(command);
    }
}
