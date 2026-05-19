package com.quizz.user.service;

import com.quizz.user.entity.User;

/**
 * Write boundary for application accounts.
 *
 * <p>Implementations create users with encoded password hashes and enforce
 * account-level invariants such as unique email. Controllers and auth flows use
 * this contract instead of writing through {@code UserRepository} directly.</p>
 */
public interface UserAccountService {

    User createUser(CreateUserCommand command);
}
