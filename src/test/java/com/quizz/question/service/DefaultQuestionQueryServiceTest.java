package com.quizz.question.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quizz.category.entity.Category;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.common.exception.NotFoundException;
import com.quizz.question.entity.AnswerOptionDraft;
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
class DefaultQuestionQueryServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    private DefaultQuestionQueryService service;

    @BeforeEach
    void setUp() {
        service = new DefaultQuestionQueryService(questionRepository);
    }

    @Test
    void getByIdReturnsQuestion() {
        Question question = question();
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        assertThat(service.getById(1L)).isSameAs(question);
    }

    @Test
    void getByIdMissingThrowsNotFoundException() {
        when(questionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Question not found.");
    }

    @Test
    void getByIdWithDetailsReturnsQuestion() {
        Question question = question();
        when(questionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(question));

        assertThat(service.getByIdWithDetails(1L)).isSameAs(question);
    }

    @Test
    void getByIdWithDetailsMissingThrowsNotFoundException() {
        when(questionRepository.findByIdWithDetails(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByIdWithDetails(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Question not found.");
    }

    @Test
    void getActiveByIdReturnsActiveQuestion() {
        Question question = question();
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        assertThat(service.getActiveById(1L)).isSameAs(question);
    }

    @Test
    void getActiveByIdArchivedThrowsBusinessRuleException() {
        Question question = question();
        question.archive();
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> service.getActiveById(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Question is archived.");
    }

    @Test
    void findAllForAdminDelegatesToRepository() {
        List<Question> questions = List.of(question());
        when(questionRepository.findAllWithDetailsForAdmin()).thenReturn(questions);

        assertThat(service.findAllForAdmin()).isSameAs(questions);
        verify(questionRepository).findAllWithDetailsForAdmin();
    }

    @Test
    void findActiveDelegatesActiveQuery() {
        service.findActive();

        verify(questionRepository).findByStatusWithCategory(QuestionStatus.ACTIVE);
    }

    @Test
    void findActiveByCategoryDelegatesCategoryAndActiveQuery() {
        service.findActiveByCategory(5L);

        verify(questionRepository).findByCategoryIdAndStatusWithCategory(5L, QuestionStatus.ACTIVE);
    }

    @Test
    void countActiveByCategoryDelegates() {
        when(questionRepository.countByCategoryIdAndStatus(5L, QuestionStatus.ACTIVE)).thenReturn(3L);

        assertThat(service.countActiveByCategory(5L)).isEqualTo(3L);
        verify(questionRepository).countByCategoryIdAndStatus(5L, QuestionStatus.ACTIVE);
    }

    private Question question() {
        return Question.create(
                "What is water?",
                Category.create("Science", null),
                List.of(new AnswerOptionDraft("H2O", true), new AnswerOptionDraft("CO2", false)));
    }
}
