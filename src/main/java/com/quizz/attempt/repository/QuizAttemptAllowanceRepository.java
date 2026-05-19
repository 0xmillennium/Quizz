package com.quizz.attempt.repository;

import com.quizz.attempt.entity.QuizAttemptAllowance;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

/**
 * Spring Data access for per-user quiz attempt allowances.
 *
 * <p>
 * Command flows use the pessimistic write query when consuming rights or
 * starting cooldown so concurrent starts/restarts serialize on the user+quiz
 * allowance row.
 * </p>
 */
public interface QuizAttemptAllowanceRepository extends JpaRepository<QuizAttemptAllowance, Long> {

    Optional<QuizAttemptAllowance> findByUserIdAndQuizId(Long userId, Long quizId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select allowance
            from QuizAttemptAllowance allowance
            where allowance.user.id = :userId
              and allowance.quiz.id = :quizId
            """)
    Optional<QuizAttemptAllowance> findByUserIdAndQuizIdForUpdate(Long userId, Long quizId);
}
