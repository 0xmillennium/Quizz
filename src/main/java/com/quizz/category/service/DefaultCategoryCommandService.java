package com.quizz.category.service;

import com.quizz.category.dto.CategoryCreateRequest;
import com.quizz.category.dto.CategoryUpdateRequest;
import com.quizz.category.entity.Category;
import com.quizz.category.repository.CategoryRepository;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.common.exception.DuplicateResourceException;
import com.quizz.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DefaultCategoryCommandService implements CategoryCommandService {

    private final CategoryRepository categoryRepository;

    public DefaultCategoryCommandService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category create(CategoryCreateRequest request) {
        if (request == null) {
            throw new BusinessRuleException("Category request is required.");
        }

        String name = normalizeName(request.getName());
        String description = normalizeDescription(request.getDescription());
        validate(name, description);

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Category name is already in use.");
        }

        return categoryRepository.save(Category.create(name, description));
    }

    @Override
    public Category update(Long categoryId, CategoryUpdateRequest request) {
        if (request == null) {
            throw new BusinessRuleException("Category request is required.");
        }

        Category category = getRequiredCategory(categoryId);
        String name = normalizeName(request.getName());
        String description = normalizeDescription(request.getDescription());
        validate(name, description);

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, categoryId)) {
            throw new DuplicateResourceException("Category name is already in use.");
        }

        category.updateDetails(name, description);
        return category;
    }

    @Override
    public void activate(Long categoryId) {
        getRequiredCategory(categoryId).activate();
    }

    @Override
    public void deactivate(Long categoryId) {
        getRequiredCategory(categoryId).deactivate();
    }

    private Category getRequiredCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found."));
    }

    private void validate(String name, String description) {
        if (name.isBlank()) {
            throw new BusinessRuleException("Category name is required.");
        }
        if (name.length() > 80) {
            throw new BusinessRuleException("Category name must not exceed 80 characters.");
        }
        if (description != null && description.length() > 500) {
            throw new BusinessRuleException("Category description must not exceed 500 characters.");
        }
    }

    private String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().replaceAll("\\s+", " ");
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }

        String normalizedDescription = description.trim();
        if (normalizedDescription.isBlank()) {
            return null;
        }
        return normalizedDescription;
    }
}
