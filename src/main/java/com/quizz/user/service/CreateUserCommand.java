package com.quizz.user.service;

import com.quizz.user.entity.UserRole;

public record CreateUserCommand(
        String fullName,
        String email,
        String passwordHash,
        UserRole role
) {
}
