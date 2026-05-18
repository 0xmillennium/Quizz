package com.quizz.quiz.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quizz.category.entity.Category;
import com.quizz.category.service.CategoryQueryService;
import com.quizz.common.entity.BaseEntity;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.question.entity.AnswerOptionDraft;
import com.quizz.question.entity.Question;
import com.quizz.question.service.QuestionQueryService;
import com.quizz.quiz.dto.QuizCreateRequest;
import com.quizz.quiz.dto.QuizUpdateRequest;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

@ExtendWith(MockitoExtension.class)
class QuizFormValidatorTest {

    @Mock
    private CategoryQueryService categoryQueryService;

    @Mock
    private QuestionQueryService questionQueryService;

    private Category category;
    private Question question;
    private QuizFormValidator validator;

    @BeforeEach
    void setUp() throws Exception {
        category = category(1L, "Science");
        question = question(10L, "Question?", category);
        validator = new QuizFormValidator(categoryQueryService, questionQueryService);
    }

    @Test
    void validRequestHasNoErrors() {
        QuizCreateRequest request = validCreateRequest();
        BindingResult bindingResult = bindingResult(request, "quizCreateRequest");
        when(categoryQueryService.getActiveById(1L)).thenReturn(category);
        when(questionQueryService.getActiveById(10L)).thenReturn(question);

        validator.validateCreate(request, bindingResult);

        assertThat(bindingResult.hasErrors()).isFalse();
    }

    @Test
    void invalidCategoryAddsCategoryIdError() {
        QuizCreateRequest request = validCreateRequest();
        BindingResult bindingResult = bindingResult(request, "quizCreateRequest");
        when(categoryQueryService.getActiveById(1L)).thenThrow(new BusinessRuleException("Category is inactive."));
        when(questionQueryService.getActiveById(10L)).thenReturn(question);

        validator.validateCreate(request, bindingResult);

        assertThat(bindingResult.getFieldError("categoryId").getCode()).isEqualTo("quiz.category.invalid");
    }

    @Test
    void categoryFieldAlreadyHasErrorDoesNotCallCategoryService() {
        QuizCreateRequest request = validCreateRequest();
        BindingResult bindingResult = bindingResult(request, "quizCreateRequest");
        bindingResult.rejectValue("categoryId", "NotNull");
        when(questionQueryService.getActiveById(10L)).thenReturn(question);

        validator.validateCreate(request, bindingResult);

        verify(categoryQueryService, never()).getActiveById(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void emptyQuestionIdsAddsError() {
        QuizCreateRequest request = validCreateRequest();
        request.setQuestionIds(List.of());
        BindingResult bindingResult = bindingResult(request, "quizCreateRequest");
        when(categoryQueryService.getActiveById(1L)).thenReturn(category);

        validator.validateCreate(request, bindingResult);

        assertThat(bindingResult.getFieldError("questionIds").getCode()).isEqualTo("quiz.questions.required");
    }

    @Test
    void duplicateQuestionIdsAddsError() {
        QuizCreateRequest request = validCreateRequest();
        request.setQuestionIds(List.of(10L, 10L));
        BindingResult bindingResult = bindingResult(request, "quizCreateRequest");
        when(categoryQueryService.getActiveById(1L)).thenReturn(category);

        validator.validateCreate(request, bindingResult);

        assertThat(bindingResult.getFieldError("questionIds").getCode()).isEqualTo("quiz.questions.duplicate");
    }

    @Test
    void invalidOrArchivedQuestionAddsQuestionIdsError() {
        QuizCreateRequest request = validCreateRequest();
        BindingResult bindingResult = bindingResult(request, "quizCreateRequest");
        when(categoryQueryService.getActiveById(1L)).thenReturn(category);
        when(questionQueryService.getActiveById(10L)).thenThrow(new BusinessRuleException("Question is archived."));

        validator.validateCreate(request, bindingResult);

        assertThat(bindingResult.getFieldError("questionIds").getCode()).isEqualTo("quiz.questions.invalid");
    }

    @Test
    void questionCountLessThanOneAddsError() {
        QuizCreateRequest request = validCreateRequest();
        request.setQuestionCount(0);
        BindingResult bindingResult = bindingResult(request, "quizCreateRequest");
        when(categoryQueryService.getActiveById(1L)).thenReturn(category);
        when(questionQueryService.getActiveById(10L)).thenReturn(question);

        validator.validateCreate(request, bindingResult);

        assertThat(bindingResult.getFieldError("questionCount").getCode()).isEqualTo("quiz.questionCount.min");
    }

    @Test
    void attemptLimitLessThanOneAddsError() {
        QuizCreateRequest request = validCreateRequest();
        request.setAttemptLimit(0);
        BindingResult bindingResult = bindingResult(request, "quizCreateRequest");
        when(categoryQueryService.getActiveById(1L)).thenReturn(category);
        when(questionQueryService.getActiveById(10L)).thenReturn(question);

        validator.validateCreate(request, bindingResult);

        assertThat(bindingResult.getFieldError("attemptLimit").getCode()).isEqualTo("quiz.attemptLimit.min");
    }

    @Test
    void retakeCooldownMinutesLessThanOneAddsError() {
        QuizCreateRequest request = validCreateRequest();
        request.setRetakeCooldownMinutes(0);
        BindingResult bindingResult = bindingResult(request, "quizCreateRequest");
        when(categoryQueryService.getActiveById(1L)).thenReturn(category);
        when(questionQueryService.getActiveById(10L)).thenReturn(question);

        validator.validateCreate(request, bindingResult);

        assertThat(bindingResult.getFieldError("retakeCooldownMinutes").getCode())
                .isEqualTo("quiz.retakeCooldownMinutes.min");
    }

    @Test
    void questionCountGreaterThanSelectedPoolSizeAddsError() {
        QuizCreateRequest request = validCreateRequest();
        request.setQuestionCount(2);
        BindingResult bindingResult = bindingResult(request, "quizCreateRequest");
        when(categoryQueryService.getActiveById(1L)).thenReturn(category);
        when(questionQueryService.getActiveById(10L)).thenReturn(question);

        validator.validateCreate(request, bindingResult);

        assertThat(bindingResult.getFieldError("questionCount").getCode()).isEqualTo("quiz.questionCount.poolSize");
    }

    @Test
    void questionIdsFieldAlreadyHasErrorDoesNotCallQuestionService() {
        QuizCreateRequest request = validCreateRequest();
        BindingResult bindingResult = bindingResult(request, "quizCreateRequest");
        bindingResult.rejectValue("questionIds", "NotEmpty");
        when(categoryQueryService.getActiveById(1L)).thenReturn(category);

        validator.validateCreate(request, bindingResult);

        verify(questionQueryService, never()).getActiveById(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void questionFromDifferentCategoryAddsQuestionIdsError() throws Exception {
        Category otherCategory = category(2L, "History");
        Question otherQuestion = question(20L, "Other?", otherCategory);
        QuizCreateRequest request = validCreateRequest();
        request.setQuestionIds(List.of(20L));
        BindingResult bindingResult = bindingResult(request, "quizCreateRequest");
        when(categoryQueryService.getActiveById(1L)).thenReturn(category);
        when(questionQueryService.getActiveById(20L)).thenReturn(otherQuestion);

        validator.validateCreate(request, bindingResult);

        assertThat(bindingResult.getFieldError("questionIds").getCode())
                .isEqualTo("quiz.questions.categoryMismatch");
    }

    @Test
    void validateUpdateBehavesSameAsCreate() {
        QuizUpdateRequest request = new QuizUpdateRequest();
        request.setTitle("Science Quiz");
        request.setCategoryId(1L);
        request.setDurationMinutes(30);
        request.setQuestionIds(List.of(10L, 10L));
        BindingResult bindingResult = bindingResult(request, "quizUpdateRequest");
        when(categoryQueryService.getActiveById(1L)).thenReturn(category);

        validator.validateUpdate(request, bindingResult);

        assertThat(bindingResult.getFieldError("questionIds").getCode()).isEqualTo("quiz.questions.duplicate");
    }

    private QuizCreateRequest validCreateRequest() {
        QuizCreateRequest request = new QuizCreateRequest();
        request.setTitle("Science Quiz");
        request.setCategoryId(1L);
        request.setDurationMinutes(30);
        request.setQuestionIds(List.of(10L));
        return request;
    }

    private Category category(Long id, String name) throws Exception {
        Category created = Category.create(name, null);
        setId(created, id);
        return created;
    }

    private Question question(Long id, String text, Category category) throws Exception {
        Question created = Question.create(
                text,
                category,
                List.of(new AnswerOptionDraft("A", true), new AnswerOptionDraft("B", false))
        );
        setId(created, id);
        return created;
    }

    private BindingResult bindingResult(Object target, String objectName) {
        return new BeanPropertyBindingResult(target, objectName);
    }

    private void setId(BaseEntity entity, Long id) throws Exception {
        Field field = BaseEntity.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}
