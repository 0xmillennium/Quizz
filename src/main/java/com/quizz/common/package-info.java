/**
 * Shared infrastructure types used across Quizz packages.
 *
 * <p>
 * This package owns cross-cutting primitives such as base entities, web
 * messages, validation error shapes, and application exceptions. It must stay
 * free of feature-specific business rules so domain packages can depend on it
 * without creating reverse dependencies.
 * </p>
 */
package com.quizz.common;
