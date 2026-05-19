package com.quizz.quiz.repository;

import com.quizz.question.entity.Question;
import com.quizz.quiz.entity.Quiz;
import com.quizz.quiz.entity.QuizStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Spring Data access for quiz definitions and their authored pools.
 *
 * <p>
 * Query methods expose explicit fetch graphs for admin views, published
 * public reads, and attempt creation. Controllers should use
 * {@code QuizQueryService} and {@code QuizCommandService} instead of injecting
 * this repository.
 * </p>
 */
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @EntityGraph(attributePaths = {
            "category",
            "questions",
            "questions.question",
            "questions.question.category"
    })
    @Query("select distinct q from Quiz q order by q.createdAt desc")
    List<Quiz> findAllWithCategoryAndQuestionsForAdmin();

    @EntityGraph(attributePaths = {"category", "questions"})
    @Query("""
            select distinct q
            from Quiz q
            where q.status = :status
            order by q.createdAt desc
            """)
    List<Quiz> findByStatusWithCategoryAndQuestions(QuizStatus status);

    @EntityGraph(attributePaths = {"category", "questions"})
    @Query("""
            select distinct q
            from Quiz q
            where q.category.id = :categoryId
              and q.status = :status
            order by q.createdAt desc
            """)
    List<Quiz> findByCategoryIdAndStatusWithCategoryAndQuestions(
            Long categoryId,
            QuizStatus status);

    @EntityGraph(attributePaths = {
            "category",
            "questions",
            "questions.question",
            "questions.question.category"
    })
    @Query("select distinct q from Quiz q where q.id = :id")
    Optional<Quiz> findByIdWithAdminDetails(Long id);

    @EntityGraph(attributePaths = {"category", "questions", "questions.question", "questions.question.category"})
    @Query("""
            select distinct q
            from Quiz q
            where q.id = :id
              and q.status = :status
            """)
    Optional<Quiz> findByIdAndStatusWithDetails(Long id, QuizStatus status);

    @EntityGraph(attributePaths = {
            "category",
            "questions",
            "questions.question",
            "questions.question.category"
    })
    @Query("""
            select distinct q
            from Quiz q
            where q.id = :id
              and q.status = :status
            """)
    Optional<Quiz> findByIdAndStatusWithAttemptGraph(
            Long id,
            QuizStatus status);

    @Query("""
            select distinct question
            from Question question
            left join fetch question.options
            where question.id in :questionIds
            """)
    List<Question> findQuestionsWithOptionsByIdIn(List<Long> questionIds);
}
