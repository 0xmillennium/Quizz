package com.onlinequiz.security.service;

import com.onlinequiz.common.exception.NotFoundException;
import com.onlinequiz.security.principal.CustomUserDetails;
import com.onlinequiz.user.entity.User;
import com.onlinequiz.user.service.UserQueryService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
