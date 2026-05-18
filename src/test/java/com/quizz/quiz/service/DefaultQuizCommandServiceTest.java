package com.quizz.quiz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
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
import com.quizz.quiz.entity.Quiz;
import com.quizz.quiz.entity.QuizStatus;
import com.quizz.quiz.repository.QuizRepository;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultQuizCommandServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private CategoryQueryService categoryQueryService;

    @Mock
    private QuestionQueryService questionQueryService;

    private Category category;
    private Question firstQuestion;
    private Question secondQuestion;
    private DefaultQuizCommandService service;

    @BeforeEach
    void setUp() throws Exception {
        category = category(1L, "Science");
        firstQuestion = question(10L, "First?", category);
        secondQuestion = question(20L, "Second?", category);
        service = new DefaultQuizCommandService(quizRepository, categoryQueryService, questionQueryService);

        lenient().when(quizRepository.save(any(Quiz.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(categoryQueryService.getActiveById(1L)).thenReturn(category);
        lenient().when(questionQueryService.getActiveById(10L)).thenReturn(firstQuestion);
        lenient().when(questionQueryService.getActiveById(20L)).thenReturn(secondQuestion);
    }

    @Test
    void createCreatesDraftQuiz() {
        Quiz quiz = service.create(createRequest("Science Quiz", "Basics", 1L, 30, 10L, 20L));

        assertThat(quiz.getStatus()).isEqualTo(QuizStatus.DRAFT);
        assertThat(quiz.getQuestions()).hasSize(2);
        assertThat(quiz.getQuestionCount()).isEqualTo(1);
        assertThat(quiz.getAttemptLimit()).isEqualTo(3);
        assertThat(quiz.getRetakeCooldownMinutes()).isEqualTo(1440);
    }

    @Test
    void createNormalizesTitle() {
        Quiz quiz = service.create(createRequest("  Science   Quiz  ", "Basics", 1L, 30, 10L));

        assertThat(quiz.getTitle()).isEqualTo("Science Quiz");
    }

    @Test
    void createNormalizesBlankDescriptionToNull() {
        Quiz quiz = service.create(createRequest("Science Quiz", "   ", 1L, 30, 10L));

        assertThat(quiz.getDescription()).isNull();
    }

    @Test
    void createUsesActiveCategoryViaCategoryQueryService() {
        service.create(createRequest("Science Quiz", null, 1L, 30, 10L));

        verify(categoryQueryService).getActiveById(1L);
    }

    @Test
    void createLoadsActiveQuestionsInRequestOrder() {
        Quiz quiz = service.create(createRequest("Science Quiz", null, 1L, 30, 20L, 10L));

        assertThat(quiz.getQuestions()).extracting(quizQuestion -> quizQuestion.getQuestion().getId())
                .containsExactly(20L, 10L);
    }

    @Test
    void createRejectsEmptyQuestionList() {
        assertThatThrownBy(() -> service.create(createRequest("Science Quiz", null, 1L, 30)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Quiz must have at least one question.");
    }

    @Test
    void createRejectsDuplicateQuestionIds() {
        assertThatThrownBy(() -> service.create(createRequest("Science Quiz", null, 1L, 30, 10L, 10L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Duplicate questions are not allowed.");
    }

    @Test
    void createRejectsDurationBelowOne() {
        assertThatThrownBy(() -> service.create(createRequest("Science Quiz", null, 1L, 0, 10L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Quiz duration must be between 1 and 180 minutes.");
    }

    @Test
    void createRejectsDurationAboveOneHundredEighty() {
        assertThatThrownBy(() -> service.create(createRequest("Science Quiz", null, 1L, 181, 10L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Quiz duration must be between 1 and 180 minutes.");
    }

    @Test
    void createRejectsQuestionCountBelowOne() {
        QuizCreateRequest request = createRequest("Science Quiz", null, 1L, 30, 10L);
        request.setQuestionCount(0);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Questions per attempt must be at least 1.");
    }

    @Test
    void createRejectsAttemptLimitBelowOne() {
        QuizCreateRequest request = createRequest("Science Quiz", null, 1L, 30, 10L);
        request.setAttemptLimit(0);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Attempt limit must be at least 1.");
    }

    @Test
    void createRejectsRetakeCooldownBelowOne() {
        QuizCreateRequest request = createRequest("Science Quiz", null, 1L, 30, 10L);
        request.setRetakeCooldownMinutes(0);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Retake cooldown must be at least 1 minute.");
    }

    @Test
    void createRejectsQuestionCountGreaterThanSelectedPoolSize() {
        QuizCreateRequest request = createRequest("Science Quiz", null, 1L, 30, 10L);
        request.setQuestionCount(2);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Questions per attempt cannot exceed the pool size.");
    }

    @Test
    void createRejectsQuestionFromDifferentCategory() throws Exception {
        Category otherCategory = category(2L, "History");
        when(questionQueryService.getActiveById(30L)).thenReturn(question(30L, "Other?", otherCategory));

        assertThatThrownBy(() -> service.create(createRequest("Science Quiz", null, 1L, 30, 30L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("All selected questions must belong to the selected category.");
    }

    @Test
    void updateDraftUpdatesOnlyDraftQuiz() {
        Quiz quiz = Quiz.create("Old", null, category, 20, 1, 3, 1440, List.of(firstQuestion));
        when(quizRepository.findByIdWithAdminDetails(1L)).thenReturn(Optional.of(quiz));

        Quiz updated = service.updateDraft(1L, updateRequest("New Title", "New", 1L, 40, 20L));

        assertThat(updated.getTitle()).isEqualTo("New Title");
        assertThat(updated.getDurationMinutes()).isEqualTo(40);
        assertThat(updated.getQuestions()).extracting(quizQuestion -> quizQuestion.getQuestion().getId())
                .containsExactly(20L);
    }

    @Test
    void updateDraftUpdatesAttemptPolicy() {
        Quiz quiz = Quiz.create("Old", null, category, 20, 1, 3, 1440, List.of(firstQuestion, secondQuestion));
        QuizUpdateRequest request = updateRequest("New Title", "New", 1L, 40, 10L, 20L);
        request.setQuestionCount(2);
        request.setAttemptLimit(5);
        request.setRetakeCooldownMinutes(60);
        when(quizRepository.findByIdWithAdminDetails(1L)).thenReturn(Optional.of(quiz));

        Quiz updated = service.updateDraft(1L, request);

        assertThat(updated.getQuestionCount()).isEqualTo(2);
        assertThat(updated.getAttemptLimit()).isEqualTo(5);
        assertThat(updated.getRetakeCooldownMinutes()).isEqualTo(60);
    }

    @Test
    void updateDraftReplacesQuizQuestionRows() {
        Quiz quiz = Quiz.create("Old", null, category, 20, 1, 3, 1440, List.of(firstQuestion, secondQuestion));
        when(quizRepository.findByIdWithAdminDetails(1L)).thenReturn(Optional.of(quiz));

        service.updateDraft(1L, updateRequest("Old", null, 1L, 20, 20L));

        assertThat(quiz.getQuestions()).hasSize(1);
        assertThat(quiz.getQuestions().get(0).getQuestion()).isSameAs(secondQuestion);
        assertThat(quiz.getQuestions().get(0).getDisplayOrder()).isEqualTo(1);
    }

    @Test
    void updateDraftRejectsPublishedQuiz() {
        Quiz quiz = Quiz.create("Old", null, category, 20, 1, 3, 1440, List.of(firstQuestion));
        quiz.publish();
        when(quizRepository.findByIdWithAdminDetails(1L)).thenReturn(Optional.of(quiz));

        assertThatThrownBy(() -> service.updateDraft(1L, updateRequest("New", null, 1L, 20, 10L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Only draft quizzes can be modified.");
    }

    @Test
    void updateDraftRejectsArchivedQuiz() {
        Quiz quiz = Quiz.create("Old", null, category, 20, 1, 3, 1440, List.of(firstQuestion));
        quiz.archive();
        when(quizRepository.findByIdWithAdminDetails(1L)).thenReturn(Optional.of(quiz));

        assertThatThrownBy(() -> service.updateDraft(1L, updateRequest("New", null, 1L, 20, 10L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Only draft quizzes can be modified.");
    }

    @Test
    void publishChangesDraftToPublished() {
        Quiz quiz = Quiz.create("Old", null, category, 20, 1, 3, 1440, List.of(firstQuestion, secondQuestion));
        when(quizRepository.findByIdWithAdminDetails(1L)).thenReturn(Optional.of(quiz));

        service.publish(1L);

        assertThat(quiz.getStatus()).isEqualTo(QuizStatus.PUBLISHED);
    }

    @Test
    void publishRejectsQuestionCountGreaterThanActivePoolSize() {
        Quiz quiz = Quiz.create("Old", null, category, 20, 2, 3, 1440, List.of(firstQuestion));
        when(quizRepository.findByIdWithAdminDetails(1L)).thenReturn(Optional.of(quiz));

        assertThatThrownBy(() -> service.publish(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Questions per attempt cannot exceed the pool size.");
    }

    @Test
    void publishRejectsQuizWithArchivedQuestion() {
        Quiz quiz = Quiz.create("Old", null, category, 20, 1, 3, 1440, List.of(firstQuestion));
        firstQuestion.archive();
        when(quizRepository.findByIdWithAdminDetails(1L)).thenReturn(Optional.of(quiz));

        assertThatThrownBy(() -> service.publish(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Quiz contains archived questions.");
    }

    @Test
    void publishRejectsQuizWithInactiveCategory() {
        Quiz quiz = Quiz.create("Old", null, category, 20, 1, 3, 1440, List.of(firstQuestion));
        category.deactivate();
        when(quizRepository.findByIdWithAdminDetails(1L)).thenReturn(Optional.of(quiz));

        assertThatThrownBy(() -> service.publish(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Quiz category is inactive.");
    }

    @Test
    void publishRejectsNonDraftQuiz() {
        Quiz quiz = Quiz.create("Old", null, category, 20, 1, 3, 1440, List.of(firstQuestion));
        quiz.publish();
        when(quizRepository.findByIdWithAdminDetails(1L)).thenReturn(Optional.of(quiz));

        assertThatThrownBy(() -> service.publish(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Only draft quizzes can be modified.");
    }

    @Test
    void archiveMarksQuizArchived() {
        Quiz quiz = Quiz.create("Old", null, category, 20, 1, 3, 1440, List.of(firstQuestion));
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));

        service.archive(1L);

        assertThat(quiz.getStatus()).isEqualTo(QuizStatus.ARCHIVED);
    }

    @Test
    void archiveIsIdempotent() {
        Quiz quiz = Quiz.create("Old", null, category, 20, 1, 3, 1440, List.of(firstQuestion));
        quiz.archive();
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));

        service.archive(1L);
        service.archive(1L);

        assertThat(quiz.getStatus()).isEqualTo(QuizStatus.ARCHIVED);
    }

    private QuizCreateRequest createRequest(
            String title,
            String description,
            Long categoryId,
            Integer durationMinutes,
            Long... questionIds
    ) {
        QuizCreateRequest request = new QuizCreateRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setCategoryId(categoryId);
        request.setDurationMinutes(durationMinutes);
        request.setQuestionIds(List.of(questionIds));
        return request;
    }

    private QuizUpdateRequest updateRequest(
            String title,
            String description,
            Long categoryId,
            Integer durationMinutes,
            Long... questionIds
    ) {
        QuizUpdateRequest request = new QuizUpdateRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setCategoryId(categoryId);
        request.setDurationMinutes(durationMinutes);
        request.setQuestionIds(List.of(questionIds));
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

    private void setId(BaseEntity entity, Long id) throws Exception {
        Field field = BaseEntity.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}
