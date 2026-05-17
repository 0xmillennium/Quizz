package com.quizz.security.context;

import com.quizz.security.principal.AuthenticatedUser;

public interface CurrentUserProvider {

    AuthenticatedUser getCurrentUser();

    Long getCurrentUserId();

    boolean isAuthenticated();
}
