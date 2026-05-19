package com.quizz.user.repository;

import com.quizz.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data access for user accounts.
 *
 * <p>
 * Use through user services rather than controller injection. Email queries
 * are case-insensitive to match the account identity boundary.
 * </p>
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
