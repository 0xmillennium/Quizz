package com.quizz.category.service;

import com.quizz.category.dto.CategoryCreateRequest;
import com.quizz.category.dto.CategoryUpdateRequest;
import com.quizz.category.entity.Category;

public interface CategoryCommandService {

    Category create(CategoryCreateRequest request);

    Category update(Long categoryId, CategoryUpdateRequest request);

    void activate(Long categoryId);

    void deactivate(Long categoryId);
}
