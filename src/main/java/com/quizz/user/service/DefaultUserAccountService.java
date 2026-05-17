package com.quizz.user.service;

import com.quizz.common.exception.BusinessRuleException;
import com.quizz.common.exception.DuplicateResourceException;
import com.quizz.user.dto.CreateUserCommand;
import com.quizz.user.entity.User;
import com.quizz.user.entity.UserRole;
import com.quizz.user.repository.UserRepository;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultUserAccountService implements UserAccountService {

    private final UserRepository userRepository;

    public DefaultUserAccountService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public User createUser(CreateUserCommand command) {
        if (command == null) {
            throw new BusinessRuleException("User creation command is required.");
        }

        String fullName = normalizeFullName(command.fullName());
        String email = normalizeEmail(command.email());
        validate(fullName, email, command.passwordHash(), command.role());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email is already registered.");
        }

        User user = switch (command.role()) {
            case USER -> User.createRegularUser(fullName, email, command.passwordHash());
            case ADMIN -> User.createAdmin(fullName, email, command.passwordHash());
        };

        return userRepository.save(user);
    }

    private void validate(String fullName, String email, String passwordHash, UserRole role) {
        if (fullName.isBlank()) {
            throw new BusinessRuleException("Full name is required.");
        }
        if (email.isBlank()) {
            throw new BusinessRuleException("Email is required.");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new BusinessRuleException("Password hash is required.");
        }
        if (role == null) {
            throw new BusinessRuleException("User role is required.");
        }
    }

    private String normalizeFullName(String fullName) {
        if (fullName == null) {
            return "";
        }
        return fullName.trim().replaceAll("\\s+", " ");
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
