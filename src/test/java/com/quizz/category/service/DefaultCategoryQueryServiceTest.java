package com.quizz.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quizz.category.entity.Category;
import com.quizz.category.repository.CategoryRepository;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.common.exception.NotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultCategoryQueryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    private DefaultCategoryQueryService service;

    @BeforeEach
    void setUp() {
        service = new DefaultCategoryQueryService(categoryRepository);
    }

    @Test
    void getByIdReturnsCategory() {
        Category category = Category.create("Science", null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThat(service.getById(1L)).isSameAs(category);
    }

    @Test
    void getByIdMissingThrowsNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category not found.");
    }

    @Test
    void getActiveByIdReturnsActiveCategory() {
        Category category = Category.create("Science", null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThat(service.getActiveById(1L)).isSameAs(category);
    }

    @Test
    void getActiveByIdInactiveThrowsBusinessRuleException() {
        Category category = Category.create("Science", null);
        category.deactivate();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> service.getActiveById(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Category is inactive.");
    }

    @Test
    void findAllDelegatesToFindAllByOrderByNameAsc() {
        List<Category> categories = List.of(Category.create("A", null));
        when(categoryRepository.findAllByOrderByNameAsc()).thenReturn(categories);

        assertThat(service.findAll()).isSameAs(categories);
    }

    @Test
    void findActiveDelegatesToFindByActiveTrueOrderByNameAsc() {
        List<Category> categories = List.of(Category.create("A", null));
        when(categoryRepository.findByActiveTrueOrderByNameAsc()).thenReturn(categories);

        assertThat(service.findActive()).isSameAs(categories);
    }

    @Test
    void existsByNameNormalizesName() {
        when(categoryRepository.existsByNameIgnoreCase("World History")).thenReturn(true);

        assertThat(service.existsByName("  World   History  ")).isTrue();
    }

    @Test
    void existsByNameExceptIdNormalizesName() {
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("World History", 1L)).thenReturn(true);

        assertThat(service.existsByNameExceptId("  World   History  ", 1L)).isTrue();
        verify(categoryRepository).existsByNameIgnoreCaseAndIdNot("World History", 1L);
    }
}
