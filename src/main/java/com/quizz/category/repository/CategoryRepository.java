package com.quizz.category.repository;

import com.quizz.category.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data access for category master data.
 *
 * <p>Controllers should depend on category services instead of this repository.
 * Name uniqueness checks are case-insensitive to preserve a stable authoring
 * identity for categories.</p>
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    @Query("""
            select count(c) > 0
            from Category c
            where lower(c.name) = lower(:name)
              and c.id <> :id
            """)
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("id") Long id);

    List<Category> findAllByOrderByNameAsc();

    List<Category> findByActiveTrueOrderByNameAsc();
}
