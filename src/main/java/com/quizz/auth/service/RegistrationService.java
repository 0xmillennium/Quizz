package com.quizz.auth.service;

import com.quizz.auth.dto.RegisterRequest;

/**
 * Registration command boundary for public account creation.
 *
 * <p>
 * The service accepts the MVC registration request, validates the account
 * creation contract through user services, and must not authenticate the user
 * or handle {@code POST /login}; that route belongs to Spring Security.
 * </p>
 */
public interface RegistrationService {

    void register(RegisterRequest request);
}
