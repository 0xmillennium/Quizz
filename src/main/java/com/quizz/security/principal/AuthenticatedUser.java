package com.quizz.security.principal;

import com.quizz.user.entity.UserRole;

public record AuthenticatedUser(
        Long id,
        String fullName,
        String email,
        UserRole role
) {

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}
