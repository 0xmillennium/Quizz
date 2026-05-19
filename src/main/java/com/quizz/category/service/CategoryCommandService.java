package com.quizz.category.service;

import com.quizz.category.dto.CategoryCreateRequest;
import com.quizz.category.dto.CategoryUpdateRequest;
import com.quizz.category.entity.Category;

/**
 * Write boundary for category master data.
 *
 * <p>
 * Command methods create and update category metadata and manage the
 * active/inactive lifecycle. Deactivation is the supported removal-like
 * transition; callers should not hard-delete categories to preserve question,
 * quiz, and attempt history.
 * </p>
 */
public interface CategoryCommandService {

    Category create(CategoryCreateRequest request);

    Category update(Long categoryId, CategoryUpdateRequest request);

    /**
     * Reactivates a category so it can participate in active authoring flows.
     */
    void activate(Long categoryId);

    /**
     * Deactivates a category without deleting historical references.
     *
     * <p>
     * Quiz publication validates active categories, so inactive categories
     * are excluded from newly publishable quiz definitions through that flow.
     * </p>
     */
    void deactivate(Long categoryId);
}
