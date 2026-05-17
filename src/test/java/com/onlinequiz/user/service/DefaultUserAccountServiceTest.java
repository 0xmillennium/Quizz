package com.onlinequiz.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.onlinequiz.common.exception.DuplicateResourceException;
import com.onlinequiz.user.dto.CreateUserCommand;
import com.onlinequiz.user.entity.User;
import com.onlinequiz.user.entity.UserRole;
import com.onlinequiz.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultUserAccountServiceTest {

    @Mock
    private UserRepository userRepository;

    private DefaultUserAccountService service;

    @BeforeEach
    void setUp() {
        service = new DefaultUserAccountService(userRepository);
        lenient().when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createUserCreatesRegularUser() {
        User user = service.createUser(new CreateUserCommand("Ada Lovelace", "ada@example.com", "hash", UserRole.USER));

        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getPasswordHash()).isEqualTo("hash");
    }

    @Test
    void createUserCreatesAdminWhenRoleAdmin() {
        User user = service.createUser(new CreateUserCommand("Grace Hopper", "grace@example.com", "hash", UserRole.ADMIN));

        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void createUserNormalizesEmailLowercase() {
        service.createUser(new CreateUserCommand("Ada Lovelace", "  ADA@Example.COM  ", "hash", UserRole.USER));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("ada@example.com");
    }

    @Test
    void createUserTrimsAndCollapsesFullName() {
        User user = service.createUser(new CreateUserCommand("  Ada   Byron   Lovelace  ", "ada@example.com", "hash", UserRole.USER));

        assertThat(user.getFullName()).isEqualTo("Ada Byron Lovelace");
    }

    @Test
    void createUserDuplicateEmailThrowsDuplicateResourceException() {
        when(userRepository.existsByEmailIgnoreCase("ada@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(
                new CreateUserCommand("Ada Lovelace", "ADA@example.com", "hash", UserRole.USER)
        )).isInstanceOf(DuplicateResourceException.class);
    }
}
