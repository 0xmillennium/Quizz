package com.quizz.attempt.repository;

import com.quizz.attempt.entity.QuizAttemptAllowance;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

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
