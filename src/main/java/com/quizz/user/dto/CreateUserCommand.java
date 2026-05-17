package com.quizz.user.dto;

import com.quizz.user.entity.UserRole;

public record CreateUserCommand(
        String fullName,
        String email,
        String passwordHash,
        UserRole role
) {
}
