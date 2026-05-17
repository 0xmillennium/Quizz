package com.quizz.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quizz.category.dto.CategoryCreateRequest;
import com.quizz.category.dto.CategoryUpdateRequest;
import com.quizz.category.entity.Category;
import com.quizz.category.repository.CategoryRepository;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.common.exception.DuplicateResourceException;
import com.quizz.common.exception.NotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultCategoryCommandServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    private DefaultCategoryCommandService service;

    @BeforeEach
    void setUp() {
        service = new DefaultCategoryCommandService(categoryRepository);
        lenient().when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createCreatesActiveCategory() {
        Category category = service.create(createRequest("History", "Past events"));

        assertThat(category.getName()).isEqualTo("History");
        assertThat(category.getDescription()).isEqualTo("Past events");
        assertThat(category.isActive()).isTrue();
    }

    @Test
    void createNormalizesNameTrimAndCollapse() {
        service.create(createRequest("  World   History  ", null));

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("World History");
    }

    @Test
    void createNormalizesBlankDescriptionToNull() {
        Category category = service.create(createRequest("Science", "   "));

        assertThat(category.getDescription()).isNull();
    }

    @Test
    void createThrowsDuplicateResourceExceptionIfDuplicateName() {
        when(categoryRepository.existsByNameIgnoreCase("Science")).thenReturn(true);

        assertThatThrownBy(() -> service.create(createRequest("Science", null)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Category name is already in use.");
    }

    @Test
    void createThrowsBusinessRuleExceptionIfNameBlank() {
        assertThatThrownBy(() -> service.create(createRequest("   ", null)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void updateUpdatesNameAndDescription() {
        Category category = Category.create("Old", "Old description");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Category updated = service.update(1L, updateRequest("  New   Name  ", "  New description  "));

        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getDescription()).isEqualTo("New description");
    }

    @Test
    void updateThrowsNotFoundExceptionIfCategoryMissing() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, updateRequest("Science", null)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category not found.");
    }

    @Test
    void updateThrowsDuplicateResourceExceptionIfAnotherCategoryHasSameName() {
        Category category = Category.create("Old", null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Science", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, updateRequest("Science", null)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Category name is already in use.");
    }

    @Test
    void activateSetsActiveTrue() {
        Category category = Category.create("Science", null);
        category.deactivate();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        service.activate(1L);

        assertThat(category.isActive()).isTrue();
    }

    @Test
    void activateIsIdempotent() {
        Category category = Category.create("Science", null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        service.activate(1L);
        service.activate(1L);

        assertThat(category.isActive()).isTrue();
    }

    @Test
    void deactivateSetsActiveFalse() {
        Category category = Category.create("Science", null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        service.deactivate(1L);

        assertThat(category.isActive()).isFalse();
    }

    @Test
    void deactivateIsIdempotent() {
        Category category = Category.create("Science", null);
        category.deactivate();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        service.deactivate(1L);
        service.deactivate(1L);

        assertThat(category.isActive()).isFalse();
    }

    private CategoryCreateRequest createRequest(String name, String description) {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName(name);
        request.setDescription(description);
        return request;
    }

    private CategoryUpdateRequest updateRequest(String name, String description) {
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName(name);
        request.setDescription(description);
        return request;
    }
}
