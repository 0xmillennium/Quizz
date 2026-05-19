package com.quizz.security.context;

import com.quizz.security.principal.AuthenticatedUser;

/**
 * Application boundary for authenticated user resolution.
 *
 * <p>
 * Controllers and services use this abstraction instead of depending on
 * Spring Security static context access. Implementations decide how to translate
 * the framework principal into Quizz's {@link AuthenticatedUser} view.
 * </p>
 */
public interface CurrentUserProvider {

    AuthenticatedUser getCurrentUser();

    Long getCurrentUserId();

    boolean isAuthenticated();
}
