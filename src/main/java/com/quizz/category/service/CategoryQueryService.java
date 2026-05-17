package com.quizz.category.service;

import com.quizz.category.entity.Category;
import java.util.List;

public interface CategoryQueryService {

    Category getById(Long categoryId);

    Category getActiveById(Long categoryId);

    List<Category> findAll();

    List<Category> findActive();

    boolean existsByName(String name);

    boolean existsByNameExceptId(String name, Long categoryId);
}
