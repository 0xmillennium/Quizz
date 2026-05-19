/**
 * User account identity, role state, and account lookup boundaries.
 *
 * <p>The user package owns the {@code User} aggregate and its repository.
 * Password hashes are persisted here for authentication, but should only cross
 * into the security adapter that builds {@code UserDetails}. The domain
 * {@code User} type intentionally does not implement Spring Security
 * interfaces.</p>
 */
package com.quizz.user;
