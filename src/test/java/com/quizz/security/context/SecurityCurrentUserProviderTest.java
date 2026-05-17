package com.quizz.security.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.quizz.common.exception.UnauthorizedOperationException;
import com.quizz.security.principal.AuthenticatedUser;
import com.quizz.security.principal.CustomUserDetails;
import com.quizz.user.entity.User;
import com.quizz.user.entity.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

class SecurityCurrentUserProviderTest {

    private SecurityCurrentUserProvider provider;

    @BeforeEach
    void setUp() {
        provider = new SecurityCurrentUserProvider();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void isAuthenticatedFalseWhenNoAuth() {
        assertThat(provider.isAuthenticated()).isFalse();
    }

    @Test
    void getCurrentUserThrowsUnauthorizedOperationExceptionWhenNoAuth() {
        assertThatThrownBy(() -> provider.getCurrentUser())
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void getCurrentUserReturnsAuthenticatedUserForCustomUserDetailsPrincipal() {
        setAuthentication();

        AuthenticatedUser currentUser = provider.getCurrentUser();

        assertThat(currentUser.id()).isEqualTo(11L);
        assertThat(currentUser.fullName()).isEqualTo("Ada Lovelace");
        assertThat(currentUser.email()).isEqualTo("ada@example.com");
        assertThat(currentUser.role()).isEqualTo(UserRole.USER);
    }

    @Test
    void getCurrentUserIdReturnsId() {
        setAuthentication();

        assertThat(provider.getCurrentUserId()).isEqualTo(11L);
    }

    private void setAuthentication() {
        User user = User.createRegularUser("Ada Lovelace", "ada@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", 11L);
        CustomUserDetails principal = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                principal.getPassword(),
                principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
