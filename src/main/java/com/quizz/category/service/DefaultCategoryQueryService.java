package com.quizz.category.service;

import com.quizz.category.entity.Category;
import com.quizz.category.repository.CategoryRepository;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.common.exception.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DefaultCategoryQueryService implements CategoryQueryService {

    private final CategoryRepository categoryRepository;

    public DefaultCategoryQueryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category getById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found."));
    }

    @Override
    public Category getActiveById(Long categoryId) {
        Category category = getById(categoryId);
        if (!category.isActive()) {
            throw new BusinessRuleException("Category is inactive.");
        }
        return category;
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    @Override
    public List<Category> findActive() {
        return categoryRepository.findByActiveTrueOrderByNameAsc();
    }

    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByNameIgnoreCase(normalizeName(name));
    }

    @Override
    public boolean existsByNameExceptId(String name, Long categoryId) {
        return categoryRepository.existsByNameIgnoreCaseAndIdNot(normalizeName(name), categoryId);
    }

    private String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().replaceAll("\\s+", " ");
    }
}
