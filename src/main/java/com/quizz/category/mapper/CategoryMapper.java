package com.quizz.category.mapper;

import com.quizz.category.dto.CategoryOptionResponse;
import com.quizz.category.dto.CategoryResponse;
import com.quizz.category.dto.CategoryUpdateRequest;
import com.quizz.category.entity.Category;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.isActive());
    }

    public List<CategoryResponse> toResponseList(List<Category> categories) {
        return categories.stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoryOptionResponse toOptionResponse(Category category) {
        return new CategoryOptionResponse(category.getId(), category.getName());
    }

    public List<CategoryOptionResponse> toOptionResponseList(List<Category> categories) {
        return categories.stream()
                .map(this::toOptionResponse)
                .toList();
    }

    public CategoryUpdateRequest toUpdateRequest(Category category) {
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName(category.getName());
        request.setDescription(category.getDescription());
        return request;
    }
}
