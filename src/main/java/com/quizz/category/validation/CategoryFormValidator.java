package com.quizz.category.validation;

import com.quizz.category.dto.CategoryCreateRequest;
import com.quizz.category.dto.CategoryUpdateRequest;
import com.quizz.category.service.CategoryQueryService;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class CategoryFormValidator {

    private final CategoryQueryService categoryQueryService;

    public CategoryFormValidator(CategoryQueryService categoryQueryService) {
        this.categoryQueryService = categoryQueryService;
    }

    public void validateCreate(CategoryCreateRequest request, BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors("name")) {
            return;
        }

        if (categoryQueryService.existsByName(request.getName())) {
            bindingResult.rejectValue(
                    "name",
                    "category.name.duplicate",
                    "Category name is already in use."
            );
        }
    }

    public void validateUpdate(Long categoryId, CategoryUpdateRequest request, BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors("name")) {
            return;
        }

        if (categoryQueryService.existsByNameExceptId(request.getName(), categoryId)) {
            bindingResult.rejectValue(
                    "name",
                    "category.name.duplicate",
                    "Category name is already in use."
            );
        }
    }
}
