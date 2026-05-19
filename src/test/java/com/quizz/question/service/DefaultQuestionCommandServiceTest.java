package com.quizz.question.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quizz.category.entity.Category;
import com.quizz.category.service.CategoryQueryService;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.common.exception.NotFoundException;
import com.quizz.question.dto.AnswerOptionRequest;
import com.quizz.question.dto.QuestionCreateRequest;
import com.quizz.question.dto.QuestionUpdateRequest;
import com.quizz.question.entity.Question;
import com.quizz.question.entity.QuestionStatus;
import com.quizz.question.repository.QuestionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultQuestionCommandServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private CategoryQueryService categoryQueryService;

    private Category category;
    private DefaultQuestionCommandService service;

    @BeforeEach
    void setUp() {
        category = Category.create("Science", null);
        service = new DefaultQuestionCommandService(questionRepository, categoryQueryService);
        lenient().when(questionRepository.save(any(Question.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(categoryQueryService.getActiveById(1L)).thenReturn(category);
    }

    @Test
    void createCreatesActiveQuestionWithOptions() {
        Question question = service
                .create(createRequest("What is water?", 1L, option("H2O", true), option("CO2", false)));

        assertThat(question.getText()).isEqualTo("What is water?");
        assertThat(question.getCategory()).isSameAs(category);
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.ACTIVE);
        assertThat(question.getOptions()).hasSize(2);
        assertThat(question.getOptions().get(0).getDisplayOrder()).isEqualTo(1);
        assertThat(question.getOptions().get(0).isCorrect()).isTrue();
    }

    @Test
    void createUsesActiveCategoryViaCategoryQueryService() {
        service.create(createRequest("What is water?", 1L, option("H2O", true), option("CO2", false)));

        verify(categoryQueryService).getActiveById(1L);
    }

    @Test
    void createPropagatesInactiveCategoryBusinessRuleException() {
        when(categoryQueryService.getActiveById(1L)).thenThrow(new BusinessRuleException("Category is inactive."));

        assertThatThrownBy(
                () -> service.create(createRequest("What is water?", 1L, option("H2O", true), option("CO2", false))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Category is inactive.");
    }

    @Test
    void createValidatesOptionCountMinimumTwo() {
        assertThatThrownBy(() -> service.create(createRequest("What is water?", 1L, option("H2O", true))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("A question must have at least 2 options.");
    }

    @Test
    void createValidatesOptionCountMaximumSix() {
        assertThatThrownBy(() -> service.create(createRequest(
                "What is water?",
                1L,
                option("A", true),
                option("B", false),
                option("C", false),
                option("D", false),
                option("E", false),
                option("F", false),
                option("G", false))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("A question can have at most 6 options.");
    }

    @Test
    void createValidatesExactlyOneCorrectOption() {
        assertThatThrownBy(
                () -> service.create(createRequest("What is water?", 1L, option("H2O", false), option("CO2", false))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Exactly one option must be marked as correct.");
    }

    @Test
    void createValidatesDuplicateOptionTextCaseInsensitively() {
        assertThatThrownBy(
                () -> service.create(createRequest("What is water?", 1L, option("H2O", true), option(" h2o ", false))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Option text must be unique.");
    }

    @Test
    void createTrimsQuestionText() {
        Question question = service
                .create(createRequest("  What is water?  ", 1L, option("H2O", true), option("CO2", false)));

        assertThat(question.getText()).isEqualTo("What is water?");
    }

    @Test
    void createNormalizesOptionText() {
        Question question = service
                .create(createRequest("What is water?", 1L, option("  Heavy   water  ", true), option("CO2", false)));

        assertThat(question.getOptions().get(0).getText()).isEqualTo("Heavy water");
    }

    @Test
    void updateReplacesOptions() {
        Question question = service
                .create(createRequest("What is water?", 1L, option("H2O", true), option("CO2", false)));
        when(questionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(question));

        Question updated = service.update(1L,
                updateRequest("Updated question", 1L, option("One", false), option("Two", true)));

        assertThat(updated.getText()).isEqualTo("Updated question");
        assertThat(updated.getOptions()).hasSize(2);
        assertThat(updated.getOptions()).extracting("text").containsExactly("One", "Two");
        assertThat(updated.getOptions().get(1).isCorrect()).isTrue();
    }

    @Test
    void updateThrowsNotFoundExceptionWhenQuestionMissing() {
        when(questionRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L,
                updateRequest("Updated question", 1L, option("One", true), option("Two", false))))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Question not found.");
    }

    @Test
    void updateThrowsBusinessRuleExceptionForInvalidOptions() {
        Question question = service
                .create(createRequest("What is water?", 1L, option("H2O", true), option("CO2", false)));
        when(questionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> service.update(1L,
                updateRequest("Updated question", 1L, option("One", false), option("Two", false))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Exactly one option must be marked as correct.");
    }

    @Test
    void archiveMarksQuestionArchived() {
        Question question = service
                .create(createRequest("What is water?", 1L, option("H2O", true), option("CO2", false)));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        service.archive(1L);

        assertThat(question.getStatus()).isEqualTo(QuestionStatus.ARCHIVED);
    }

    @Test
    void archiveIsIdempotent() {
        Question question = service
                .create(createRequest("What is water?", 1L, option("H2O", true), option("CO2", false)));
        question.archive();
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        service.archive(1L);
        service.archive(1L);

        assertThat(question.getStatus()).isEqualTo(QuestionStatus.ARCHIVED);
    }

    @Test
    void restoreMarksQuestionActive() {
        Question question = service
                .create(createRequest("What is water?", 1L, option("H2O", true), option("CO2", false)));
        question.archive();
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        service.restore(1L);

        assertThat(question.getStatus()).isEqualTo(QuestionStatus.ACTIVE);
    }

    @Test
    void restoreIsIdempotent() {
        Question question = service
                .create(createRequest("What is water?", 1L, option("H2O", true), option("CO2", false)));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        service.restore(1L);
        service.restore(1L);

        assertThat(question.getStatus()).isEqualTo(QuestionStatus.ACTIVE);
    }

    private QuestionCreateRequest createRequest(String text, Long categoryId, AnswerOptionRequest... options) {
        QuestionCreateRequest request = new QuestionCreateRequest();
        request.setText(text);
        request.setCategoryId(categoryId);
        request.setOptions(List.of(options));
        return request;
    }

    private QuestionUpdateRequest updateRequest(String text, Long categoryId, AnswerOptionRequest... options) {
        QuestionUpdateRequest request = new QuestionUpdateRequest();
        request.setText(text);
        request.setCategoryId(categoryId);
        request.setOptions(List.of(options));
        return request;
    }

    private AnswerOptionRequest option(String text, boolean correct) {
        AnswerOptionRequest option = new AnswerOptionRequest();
        option.setText(text);
        option.setCorrect(correct);
        return option;
    }
}
