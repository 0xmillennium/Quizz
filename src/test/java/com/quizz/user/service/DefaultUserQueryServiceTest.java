package com.quizz.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quizz.common.exception.NotFoundException;
import com.quizz.user.entity.User;
import com.quizz.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultUserQueryServiceTest {

    @Mock
    private UserRepository userRepository;

    private DefaultUserQueryService service;

    @BeforeEach
    void setUp() {
        service = new DefaultUserQueryService(userRepository);
    }

    @Test
    void existsByEmailNormalizesEmail() {
        when(userRepository.existsByEmailIgnoreCase("ada@example.com")).thenReturn(true);

        assertThat(service.existsByEmail("  ADA@Example.COM  ")).isTrue();
        verify(userRepository).existsByEmailIgnoreCase("ada@example.com");
    }

    @Test
    void getByEmailReturnsUser() {
        User user = User.createRegularUser("Ada Lovelace", "ada@example.com", "hash");
        when(userRepository.findByEmailIgnoreCase("ada@example.com")).thenReturn(Optional.of(user));

        assertThat(service.getByEmail("ada@example.com")).isSameAs(user);
    }

    @Test
    void getByEmailMissingThrowsNotFoundException() {
        when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByEmail("missing@example.com"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getByIdReturnsUser() {
        User user = User.createRegularUser("Ada Lovelace", "ada@example.com", "hash");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThat(service.getById(1L)).isSameAs(user);
    }

    @Test
    void getByIdMissingThrowsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(NotFoundException.class);
    }
}
