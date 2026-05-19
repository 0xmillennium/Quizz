package com.quizz.user.service;

import com.quizz.user.entity.User;

/**
 * Read boundary for application accounts.
 *
 * <p>Callers use this service to resolve users by id or email without depending
 * on repository details. The returned {@link User} may contain a password hash,
 * so web-facing DTOs and reporting code must not expose it.</p>
 */
public interface UserQueryService {

    boolean existsByEmail(String email);

    User getByEmail(String email);

    User getById(Long id);
}
