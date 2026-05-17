package com.quizz.attempt.repository;

import com.quizz.attempt.entity.AttemptStatus;
import com.quizz.attempt.entity.QuizAttempt;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    @EntityGraph(attributePaths = {
            "user",
            "quiz",
            "questions",
            "questions.options"
    })
    @Query("""
            select a
            from QuizAttempt a
            where a.id = :attemptId
              and a.user.id = :userId
            """)
    Optional<QuizAttempt> findByIdAndUserIdWithQuestionsAndOptions(
            Long attemptId,
            Long userId
    );

    @EntityGraph(attributePaths = {
            "questions",
            "questions.options"
    })
    @Query("""
            select a
            from QuizAttempt a
            where a.id = :attemptId
              and a.user.id = :userId
            """)
    Optional<QuizAttempt> findResultByIdAndUserId(
            Long attemptId,
            Long userId
    );

    @Query("""
            select a
            from QuizAttempt a
            where a.user.id = :userId
            order by a.startedAt desc
            """)
    List<QuizAttempt> findHistoryByUserId(Long userId);

    @Query("""
            select a
            from QuizAttempt a
            where a.status = :status
              and a.expiresAt <= :now
            """)
    List<QuizAttempt> findByStatusAndExpiresAtBeforeOrAt(
            AttemptStatus status,
            Instant now
    );

    @Query("""
            select a
            from QuizAttempt a
            where a.user.id = :userId
              and a.quiz.id = :quizId
              and a.status = :status
            """)
    Optional<QuizAttempt> findByUserIdAndQuizIdAndStatus(
            Long userId,
            Long quizId,
            AttemptStatus status
    );
}
