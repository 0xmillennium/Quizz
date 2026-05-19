package com.quizz.category.service;

import com.quizz.category.entity.Category;
import java.util.List;

/**
 * Read boundary for category master data.
 *
 * <p>Query methods provide all-category and active-category views for admin,
 * question authoring, quiz authoring, and public filtering. Implementations
 * must not mutate category lifecycle state.</p>
 */
public interface CategoryQueryService {

    Category getById(Long categoryId);

    Category getActiveById(Long categoryId);

    List<Category> findAll();

    List<Category> findActive();

    boolean existsByName(String name);

    boolean existsByNameExceptId(String name, Long categoryId);
}
