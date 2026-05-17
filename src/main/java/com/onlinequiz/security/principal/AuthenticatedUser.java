package com.onlinequiz.security.principal;

import com.onlinequiz.user.entity.UserRole;

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
