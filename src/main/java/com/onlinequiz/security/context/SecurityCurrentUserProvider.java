package com.onlinequiz.security.context;

import com.onlinequiz.common.exception.UnauthorizedOperationException;
import com.onlinequiz.security.principal.AuthenticatedUser;
import com.onlinequiz.security.principal.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

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
                principal.getRole()
        );
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
