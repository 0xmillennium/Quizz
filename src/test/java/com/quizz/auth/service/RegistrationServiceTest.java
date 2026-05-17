package com.quizz.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quizz.auth.dto.RegisterRequest;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.common.exception.DuplicateResourceException;
import com.quizz.user.dto.CreateUserCommand;
import com.quizz.user.entity.UserRole;
import com.quizz.user.service.UserAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserAccountService userAccountService;

    private DefaultRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new DefaultRegistrationService(passwordEncoder, userAccountService);
    }

    @Test
    void registerEncodesPassword() {
        RegisterRequest request = request("password123", "password123");
        when(passwordEncoder.encode("password123")).thenReturn("encoded");

        service.register(request);

        verify(passwordEncoder).encode("password123");
    }

    @Test
    void registerCreatesCreateUserCommandWithUserRole() {
        RegisterRequest request = request("password123", "password123");
        when(passwordEncoder.encode("password123")).thenReturn("encoded");

        service.register(request);

        ArgumentCaptor<CreateUserCommand> captor = ArgumentCaptor.forClass(CreateUserCommand.class);
        verify(userAccountService).createUser(captor.capture());
        assertThat(captor.getValue().fullName()).isEqualTo("Ada Lovelace");
        assertThat(captor.getValue().email()).isEqualTo("ada@example.com");
        assertThat(captor.getValue().passwordHash()).isEqualTo("encoded");
        assertThat(captor.getValue().role()).isEqualTo(UserRole.USER);
    }

    @Test
    void passwordMismatchThrowsBusinessRuleException() {
        RegisterRequest request = request("password123", "different123");

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void duplicateResourceExceptionFromUserAccountServicePropagates() {
        RegisterRequest request = request("password123", "password123");
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userAccountService.createUser(any(CreateUserCommand.class)))
                .thenThrow(new DuplicateResourceException("duplicate"));

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    private RegisterRequest request(String password, String confirmPassword) {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Ada Lovelace");
        request.setEmail("ada@example.com");
        request.setPassword(password);
        request.setConfirmPassword(confirmPassword);
        return request;
    }
}
