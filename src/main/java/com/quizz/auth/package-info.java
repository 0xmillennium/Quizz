/**
 * Authentication-facing MVC flows that are not owned by Spring Security itself.
 *
 * <p>This package handles registration and login page rendering. The
 * {@code POST /login} credential exchange is handled by Spring Security, while
 * registration delegates user creation to service abstractions instead of
 * reaching into repositories.</p>
 */
package com.quizz.auth;
