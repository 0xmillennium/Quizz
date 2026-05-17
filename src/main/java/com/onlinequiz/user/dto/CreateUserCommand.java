package com.onlinequiz.user.dto;

import com.onlinequiz.user.entity.UserRole;

public record CreateUserCommand(
        String fullName,
        String email,
        String passwordHash,
        UserRole role
) {
}
