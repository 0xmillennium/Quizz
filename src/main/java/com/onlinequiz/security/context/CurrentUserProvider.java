package com.onlinequiz.security.context;

import com.onlinequiz.security.principal.AuthenticatedUser;

public interface CurrentUserProvider {

    AuthenticatedUser getCurrentUser();

    Long getCurrentUserId();

    boolean isAuthenticated();
}
