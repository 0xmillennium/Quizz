package com.quizz.security.context;

import com.quizz.common.exception.UnauthorizedOperationException;
import com.quizz.security.principal.AuthenticatedUser;
import com.quizz.security.principal.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Spring Security-backed current-user provider.
 *
 * <p>
 * This adapter is the production boundary that reads
 * {@code SecurityContextHolder}. It converts the authenticated
 * {@link CustomUserDetails} principal into an {@link AuthenticatedUser} record
 * so MVC and service code do not depend on framework principal types.
 * </p>
 */
@Component
public class SecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isCustomUserAuthentication(authentication)) {
            throw new UnauthorizedOperationException("Authentication is required.");
        }

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return new AuthenticatedUser(
                principal.getId(),
                principal.getFullName(),
                principal.getEmail(),
                principal.getRole());
    }

    @Override
    public Long getCurrentUserId() {
        return getCurrentUser().id();
    }

    @Override
    public boolean isAuthenticated() {
        return isCustomUserAuthentication(SecurityContextHolder.getContext().getAuthentication());
    }

    private boolean isCustomUserAuthentication(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof CustomUserDetails;
    }
}
