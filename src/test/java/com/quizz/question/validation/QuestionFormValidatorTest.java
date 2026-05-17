package com.quizz.question.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quizz.category.entity.Category;
import com.quizz.category.service.CategoryQueryService;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.question.dto.AnswerOptionRequest;
import com.quizz.question.dto.QuestionCreateRequest;
import com.quizz.question.dto.QuestionUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

@ExtendWith(MockitoExtension.class)
class QuestionFormValidatorTest {

    @Mock
    private CategoryQueryService categoryQueryService;

    private QuestionFormValidator validator;

    @BeforeEach
    void setUp() {
        validator = new QuestionFormValidator(categoryQueryService);
    }

    @Test
    void validRequestHasNoErrors() {
        QuestionCreateRequest request = validCreateRequest();
        BindingResult bindingResult = bindingResult(request, "questionCreateRequest");

        validator.validateCreate(request, bindingResult);

        assertThat(bindingResult.hasErrors()).isFalse();
    }

    @Test
    void invalidCategoryAddsCategoryIdError() {
        QuestionCreateRequest request = validCreateRequest();
        BindingResult bindingResult = bindingResult(request, "questionCreateRequest");
        when(categoryQueryService.getActiveById(1L)).thenThrow(new BusinessRuleException("Category is inactive."));

        validator.validateCreate(request, bindingResult);

        assertThat(bindingResult.getFieldError("categoryId")).isNotNull();
        assertThat(bindingResult.getFieldError("categoryId").getCode()).isEqualTo("question.category.invalid");
    }

    @Test
    void categoryFieldAlreadyHasErrorDoesNotCallCategoryService() {
        QuestionCreateRequest request = validCreateRequest();
        BindingResult bindingResult = bindingResult(request, "questionCreateRequest");
        bindingResult.rejectValue("categoryId", "NotNull");

        validator.validateCreate(request, bindingResult);

        verify(categoryQueryService, never()).getActiveById(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void optionCountBelowTwoAddsOptionsError() {
        QuestionCreateRequest request = createRequest(option("Only", true));
        BindingResult bindingResult = bindingResult(request, "questionCreateRequest");

        validator.validateCreate(request, bindingResult);

        assertThat(bindingResult.getFieldError("options").getCode()).isEqualTo("question.options.count");
    }

    @Test
    void optionCountAboveSixAddsOptionsError() {
        QuestionCreateRequest request = createRequest(
                option("A", true),
                option("B", false),
                option("C", false),
                option("D", false),
                option("E", false),
                option("F", false),
                option("G", false)
        );
        BindingResult bindingResult = bindingResult(request, "questionCreateRequest");

        validator.validateCreate(request, bindingResult);

        assertThat(bindingResult.getFieldError("options").getCode()).isEqualTo("question.options.count");
    }

    @Test
    void zeroCorrectOptionAddsOptionsError() {
        QuestionCreateRequest request = createRequest(option("A", false), option("B", false));
        BindingResult bindingResult = bindingResult(request, "questionCreateRequest");

        validator.validateCreate(request, bindingResult);

        assertThat(bindingResult.getFieldError("options").getCode()).isEqualTo("question.options.correctRequired");
    }

    @Test
    void multipleCorrectOptionsAddsOptionsError() {
        QuestionCreateRequest request = createRequest(option("A", true), option("B", true));
        BindingResult bindingResult = bindingResult(request, "questionCreateRequest");

        validator.validateCreate(request, bindingResult);

        assertThat(bindingResult.getFieldError("options").getCode()).isEqualTo("question.options.correctRequired");
    }

    @Test
    void duplicateOptionTextAddsFieldError() {
        QuestionCreateRequest request = createRequest(option("New   York", true), option(" new york ", false));
        BindingResult bindingResult = bindingResult(request, "questionCreateRequest");

        validator.validateCreate(request, bindingResult);

        assertThat(bindingResult.getFieldError("options[1].text").getCode()).isEqualTo("question.options.duplicate");
    }

    @Test
    void blankOptionTextAddsFieldError() {
        QuestionCreateRequest request = createRequest(option("A", true), option("   ", false));
        BindingResult bindingResult = bindingResult(request, "questionCreateRequest");

        validator.validateCreate(request, bindingResult);

        assertThat(bindingResult.getFieldError("options[1].text").getCode()).isEqualTo("question.options.textRequired");
    }

    @Test
    void validateUpdateBehavesSameAsCreate() {
        QuestionUpdateRequest request = new QuestionUpdateRequest();
        request.setText("What is water?");
        request.setCategoryId(1L);
        request.setOptions(java.util.List.of(option("A", true), option(" a ", false)));
        BindingResult bindingResult = bindingResult(request, "questionUpdateRequest");

        validator.validateUpdate(request, bindingResult);

        assertThat(bindingResult.getFieldError("options[1].text").getCode()).isEqualTo("question.options.duplicate");
    }

    private QuestionCreateRequest validCreateRequest() {
        return createRequest(option("H2O", true), option("CO2", false));
    }

    private QuestionCreateRequest createRequest(AnswerOptionRequest... options) {
        QuestionCreateRequest request = new QuestionCreateRequest();
        request.setText("What is water?");
        request.setCategoryId(1L);
        request.setOptions(java.util.List.of(options));
        return request;
    }

    private AnswerOptionRequest option(String text, boolean correct) {
        AnswerOptionRequest option = new AnswerOptionRequest();
        option.setText(text);
        option.setCorrect(correct);
        return option;
    }

    private BindingResult bindingResult(Object target, String objectName) {
        return new BeanPropertyBindingResult(target, objectName);
    }

    @SuppressWarnings("unused")
    private Category activeCategory() {
        return Category.create("Science", null);
    }
}
