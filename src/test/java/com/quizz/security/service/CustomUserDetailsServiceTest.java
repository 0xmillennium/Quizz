package com.quizz.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.quizz.common.exception.NotFoundException;
import com.quizz.user.entity.User;
import com.quizz.user.service.UserQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserQueryService userQueryService;

    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        service = new CustomUserDetailsService(userQueryService);
    }

    @Test
    void loadsUserByEmail() {
        User user = User.createRegularUser("Ada Lovelace", "ada@example.com", "hash");
        when(userQueryService.getByEmail("ada@example.com")).thenReturn(user);

        UserDetails userDetails = service.loadUserByUsername("ada@example.com");

        assertThat(userDetails.getUsername()).isEqualTo("ada@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("hash");
    }

    @Test
    void mapsRoleUser() {
        User user = User.createRegularUser("Ada Lovelace", "ada@example.com", "hash");
        when(userQueryService.getByEmail("ada@example.com")).thenReturn(user);

        UserDetails userDetails = service.loadUserByUsername("ada@example.com");

        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void mapsRoleAdmin() {
        User user = User.createAdmin("Grace Hopper", "grace@example.com", "hash");
        when(userQueryService.getByEmail("grace@example.com")).thenReturn(user);

        UserDetails userDetails = service.loadUserByUsername("grace@example.com");

        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void disabledUserReturnsUserDetailsWithEnabledFalse() {
        User user = User.createRegularUser("Ada Lovelace", "ada@example.com", "hash");
        user.disable();
        when(userQueryService.getByEmail("ada@example.com")).thenReturn(user);

        UserDetails userDetails = service.loadUserByUsername("ada@example.com");

        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    void missingUserThrowsUsernameNotFoundException() {
        when(userQueryService.getByEmail("missing@example.com")).thenThrow(new NotFoundException("missing"));

        assertThatThrownBy(() -> service.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
