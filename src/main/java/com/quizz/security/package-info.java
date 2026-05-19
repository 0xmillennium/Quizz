/**
 * Spring Security integration and current-user abstractions.
 *
 * <p>
 * The security package adapts Quizz account data to Spring Security and
 * exposes authenticated-user state through a small application boundary.
 * Domain entities do not implement Spring Security interfaces, and controllers
 * or services should use {@link com.quizz.security.context.CurrentUserProvider}
 * instead of reading the framework context directly.
 * </p>
 */
package com.quizz.security;
