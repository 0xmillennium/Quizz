package com.quizz.attempt.repository;

import com.quizz.attempt.entity.AttemptQuestion;
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
            "questions"
    })
    @Query("""
            select a
            from QuizAttempt a
            where a.id = :attemptId
              and a.user.id = :userId
            """)
    Optional<QuizAttempt> findByIdAndUserIdWithQuestions(
            Long attemptId,
            Long userId
    );

    @EntityGraph(attributePaths = {
            "questions"
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
            select distinct q
            from AttemptQuestion q
            left join fetch q.options
            where q.id in :questionIds
            """)
    List<AttemptQuestion> findQuestionsWithOptionsByIdIn(List<Long> questionIds);

    @Query("""
            select a
            from QuizAttempt a
            where a.user.id = :userId
            order by a.startedAt desc
            """)
    List<QuizAttempt> findHistoryByUserId(Long userId);

    @EntityGraph(attributePaths = {
            "user",
            "quiz",
            "questions"
    })
    @Query("""
            select a
            from QuizAttempt a
            where a.user.id = :userId
              and a.status = :status
              and a.expiresAt <= :now
            """)
    List<QuizAttempt> findByUserIdAndStatusAndExpiresAtLessThanEqual(
            Long userId,
            AttemptStatus status,
            Instant now
    );

    @EntityGraph(attributePaths = {
            "user",
            "quiz",
            "questions"
    })
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

    boolean existsByUserIdAndQuizIdAndStatus(Long userId, Long quizId, AttemptStatus status);

    Optional<QuizAttempt> findFirstByUserIdAndQuizIdAndStatusOrderBySubmittedAtDescStartedAtDesc(
            Long userId,
            Long quizId,
            AttemptStatus status
    );
}
