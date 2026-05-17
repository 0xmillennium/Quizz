package com.quizz.quiz.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.quizz.category.entity.Category;
import com.quizz.common.entity.BaseEntity;
import com.quizz.question.entity.AnswerOptionDraft;
import com.quizz.question.entity.Question;
import com.quizz.quiz.dto.QuizAdminResponse;
import com.quizz.quiz.dto.QuizDetailResponse;
import com.quizz.quiz.dto.QuizSummaryResponse;
import com.quizz.quiz.dto.QuizUpdateRequest;
import com.quizz.quiz.entity.Quiz;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuizMapperTest {

    private QuizMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new QuizMapper();
    }

    @Test
    void toAdminResponseMapsFieldsAndQuestions() throws Exception {
        Quiz quiz = quiz();

        QuizAdminResponse response = mapper.toAdminResponse(quiz);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.title()).isEqualTo("Science Quiz");
        assertThat(response.description()).isEqualTo("Basics");
        assertThat(response.categoryId()).isEqualTo(1L);
        assertThat(response.categoryName()).isEqualTo("Science");
        assertThat(response.durationMinutes()).isEqualTo(30);
        assertThat(response.status()).isEqualTo("DRAFT");
        assertThat(response.questionCount()).isEqualTo(2);
        assertThat(response.questions()).hasSize(2);
        assertThat(response.questions().get(0).questionId()).isEqualTo(10L);
        assertThat(response.questions().get(0).displayOrder()).isEqualTo(1);
    }

    @Test
    void toSummaryResponseMapsSummary() throws Exception {
        QuizSummaryResponse response = mapper.toSummaryResponse(quiz());

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.title()).isEqualTo("Science Quiz");
        assertThat(response.categoryName()).isEqualTo("Science");
        assertThat(response.durationMinutes()).isEqualTo(30);
        assertThat(response.questionCount()).isEqualTo(2);
    }

    @Test
    void toDetailResponseMapsDetailsAndQuestionsWithoutOptionsOrCorrectAnswers() throws Exception {
        QuizDetailResponse response = mapper.toDetailResponse(quiz());

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.title()).isEqualTo("Science Quiz");
        assertThat(response.description()).isEqualTo("Basics");
        assertThat(response.questions()).hasSize(2);
        assertThat(Arrays.stream(response.questions().get(0).getClass().getRecordComponents())
                .map(component -> component.getName()))
                .containsExactly("questionId", "text", "categoryName", "displayOrder");
    }

    @Test
    void toUpdateRequestMapsFieldsAndQuestionIdsInDisplayOrder() throws Exception {
        QuizUpdateRequest request = mapper.toUpdateRequest(quiz());

        assertThat(request.getTitle()).isEqualTo("Science Quiz");
        assertThat(request.getDescription()).isEqualTo("Basics");
        assertThat(request.getCategoryId()).isEqualTo(1L);
        assertThat(request.getDurationMinutes()).isEqualTo(30);
        assertThat(request.getQuestionIds()).containsExactly(10L, 20L);
    }

    @Test
    void toAdminResponseListMapsList() throws Exception {
        assertThat(mapper.toAdminResponseList(List.of(quiz()))).hasSize(1);
    }

    @Test
    void toSummaryResponseListMapsList() throws Exception {
        assertThat(mapper.toSummaryResponseList(List.of(quiz()))).hasSize(1);
    }

    private Quiz quiz() throws Exception {
        Category category = Category.create("Science", null);
        setId(category, 1L);
        Question first = question(10L, "First?", category);
        Question second = question(20L, "Second?", category);
        Quiz quiz = Quiz.create("Science Quiz", "Basics", category, 30, List.of(first, second));
        setId(quiz, 100L);
        return quiz;
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
