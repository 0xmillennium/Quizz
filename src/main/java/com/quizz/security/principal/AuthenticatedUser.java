package com.quizz.security.principal;

import com.quizz.user.entity.UserRole;

/**
 * Framework-free view of the authenticated account.
 *
 * <p>
 * Controllers and services use this record when they need user identity or
 * role state without depending on Spring Security principal APIs.
 * </p>
 */
public record AuthenticatedUser(
        Long id,
        String fullName,
        String email,
        UserRole role) {

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}
