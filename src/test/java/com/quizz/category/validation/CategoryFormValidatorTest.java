package com.quizz.category.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quizz.category.dto.CategoryCreateRequest;
import com.quizz.category.dto.CategoryUpdateRequest;
import com.quizz.category.service.CategoryQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

@ExtendWith(MockitoExtension.class)
class CategoryFormValidatorTest {

    @Mock
    private CategoryQueryService categoryQueryService;

    private CategoryFormValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CategoryFormValidator(categoryQueryService);
    }

    @Test
    void validateCreateAddsDuplicateErrorWhenNameExists() {
        CategoryCreateRequest request = createRequest("Science");
        BindingResult bindingResult = bindingResult(request, "categoryCreateRequest");
        when(categoryQueryService.existsByName("Science")).thenReturn(true);

        validator.validateCreate(request, bindingResult);

        assertThat(bindingResult.getFieldError("name")).isNotNull();
        assertThat(bindingResult.getFieldError("name").getCode()).isEqualTo("category.name.duplicate");
    }

    @Test
    void validateCreateDoesNotCheckDuplicateIfNameFieldAlreadyHasErrors() {
        CategoryCreateRequest request = createRequest("");
        BindingResult bindingResult = bindingResult(request, "categoryCreateRequest");
        bindingResult.rejectValue("name", "NotBlank");

        validator.validateCreate(request, bindingResult);

        verify(categoryQueryService, never()).existsByName(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void validateCreateNoErrorWhenNameUnique() {
        CategoryCreateRequest request = createRequest("Science");
        BindingResult bindingResult = bindingResult(request, "categoryCreateRequest");
        when(categoryQueryService.existsByName("Science")).thenReturn(false);

        validator.validateCreate(request, bindingResult);

        assertThat(bindingResult.hasErrors()).isFalse();
    }

    @Test
    void validateUpdateAddsDuplicateErrorWhenAnotherCategoryHasSameName() {
        CategoryUpdateRequest request = updateRequest("Science");
        BindingResult bindingResult = bindingResult(request, "categoryUpdateRequest");
        when(categoryQueryService.existsByNameExceptId("Science", 1L)).thenReturn(true);

        validator.validateUpdate(1L, request, bindingResult);

        assertThat(bindingResult.getFieldError("name")).isNotNull();
        assertThat(bindingResult.getFieldError("name").getCode()).isEqualTo("category.name.duplicate");
    }

    @Test
    void validateUpdateNoErrorWhenUnique() {
        CategoryUpdateRequest request = updateRequest("Science");
        BindingResult bindingResult = bindingResult(request, "categoryUpdateRequest");
        when(categoryQueryService.existsByNameExceptId("Science", 1L)).thenReturn(false);

        validator.validateUpdate(1L, request, bindingResult);

        assertThat(bindingResult.hasErrors()).isFalse();
    }

    @Test
    void validateUpdateDoesNotCheckDuplicateIfNameFieldAlreadyHasErrors() {
        CategoryUpdateRequest request = updateRequest("");
        BindingResult bindingResult = bindingResult(request, "categoryUpdateRequest");
        bindingResult.rejectValue("name", "NotBlank");

        validator.validateUpdate(1L, request, bindingResult);

        verify(categoryQueryService, never()).existsByNameExceptId(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    private CategoryCreateRequest createRequest(String name) {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName(name);
        return request;
    }

    private CategoryUpdateRequest updateRequest(String name) {
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName(name);
        return request;
    }

    private BindingResult bindingResult(Object target, String objectName) {
        return new BeanPropertyBindingResult(target, objectName);
    }
}
