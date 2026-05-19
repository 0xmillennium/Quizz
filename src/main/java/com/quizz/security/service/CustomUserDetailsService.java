package com.quizz.security.service;

import com.quizz.common.exception.NotFoundException;
import com.quizz.security.principal.CustomUserDetails;
import com.quizz.user.entity.User;
import com.quizz.user.service.UserQueryService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bridges Quizz user accounts to Spring Security authentication.
 *
 * <p>The domain {@link User} entity does not implement {@code UserDetails}.
 * This service resolves the account through {@link UserQueryService} and wraps
 * it in {@link CustomUserDetails} for the authentication provider.</p>
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserQueryService userQueryService;

    public CustomUserDetailsService(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            User user = userQueryService.getByEmail(username);
            return new CustomUserDetails(user);
        } catch (NotFoundException exception) {
            throw new UsernameNotFoundException("User not found.", exception);
        }
    }
}
