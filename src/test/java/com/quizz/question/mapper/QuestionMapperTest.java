package com.quizz.question.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.quizz.category.entity.Category;
import com.quizz.common.entity.BaseEntity;
import com.quizz.question.dto.QuestionResponse;
import com.quizz.question.dto.QuestionSelectionResponse;
import com.quizz.question.dto.QuestionSummaryResponse;
import com.quizz.question.dto.QuestionUpdateRequest;
import com.quizz.question.entity.AnswerOption;
import com.quizz.question.entity.AnswerOptionDraft;
import com.quizz.question.entity.Question;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuestionMapperTest {

    private QuestionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new QuestionMapper();
    }

    @Test
    void toResponseMapsQuestionAndOptions() throws Exception {
        Question question = question();

        QuestionResponse response = mapper.toResponse(question);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.text()).isEqualTo("What is water?");
        assertThat(response.categoryId()).isEqualTo(20L);
        assertThat(response.categoryName()).isEqualTo("Science");
        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.options()).hasSize(2);
        assertThat(response.options().get(0).id()).isEqualTo(101L);
        assertThat(response.options().get(0).text()).isEqualTo("H2O");
        assertThat(response.options().get(0).correct()).isTrue();
        assertThat(response.options().get(0).displayOrder()).isEqualTo(1);
    }

    @Test
    void toSummaryResponseMapsSummary() throws Exception {
        Question question = question();

        QuestionSummaryResponse response = mapper.toSummaryResponse(question);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.text()).isEqualTo("What is water?");
        assertThat(response.categoryName()).isEqualTo("Science");
        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.optionCount()).isEqualTo(2);
    }

    @Test
    void toSelectionResponseMapsWithoutOptionsOrCorrectAnswer() throws Exception {
        Question question = question();

        QuestionSelectionResponse response = mapper.toSelectionResponse(question);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.text()).isEqualTo("What is water?");
        assertThat(response.categoryName()).isEqualTo("Science");
        assertThat(Arrays.stream(QuestionSelectionResponse.class.getRecordComponents())
                .map(component -> component.getName()))
                .containsExactly("id", "text", "categoryName");
    }

    @Test
    void toSummaryResponseListMapsList() throws Exception {
        assertThat(mapper.toSummaryResponseList(List.of(question()))).hasSize(1);
    }

    @Test
    void toSelectionResponseListMapsList() throws Exception {
        assertThat(mapper.toSelectionResponseList(List.of(question()))).hasSize(1);
    }

    @Test
    void toUpdateRequestMapsEditableFields() throws Exception {
        QuestionUpdateRequest request = mapper.toUpdateRequest(question());

        assertThat(request.getText()).isEqualTo("What is water?");
        assertThat(request.getCategoryId()).isEqualTo(20L);
        assertThat(request.getOptions()).hasSize(2);
        assertThat(request.getOptions().get(0).getText()).isEqualTo("H2O");
        assertThat(request.getOptions().get(0).isCorrect()).isTrue();
    }

    private Question question() throws Exception {
        Category category = Category.create("Science", null);
        setId(category, 20L);

        Question question = Question.create(
                "What is water?",
                category,
                List.of(new AnswerOptionDraft("H2O", true), new AnswerOptionDraft("CO2", false))
        );
        setId(question, 10L);
        setId(question.getOptions().get(0), 101L);
        setId(question.getOptions().get(1), 102L);
        return question;
    }

    private void setId(BaseEntity entity, Long id) throws Exception {
        Field field = BaseEntity.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}
