package com.quizz.question.repository;

import com.quizz.question.entity.Question;
import com.quizz.question.entity.QuestionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @EntityGraph(attributePaths = {"category", "options"})
    @Query("select q from Question q order by q.createdAt desc")
    List<Question> findAllWithDetailsForAdmin();

    @EntityGraph(attributePaths = {"category", "options"})
    @Query("select q from Question q where q.id = :id")
    Optional<Question> findByIdWithDetails(Long id);

    @EntityGraph(attributePaths = {"category"})
    @Query("""
            select q
            from Question q
            where q.status = :status
            order by q.createdAt desc
            """)
    List<Question> findByStatusWithCategory(QuestionStatus status);

    @EntityGraph(attributePaths = {"category"})
    @Query("""
            select q
            from Question q
            where q.category.id = :categoryId
              and q.status = :status
            order by q.createdAt desc
            """)
    List<Question> findByCategoryIdAndStatusWithCategory(
            Long categoryId,
            QuestionStatus status
    );

    long countByCategoryIdAndStatus(Long categoryId, QuestionStatus status);
}
