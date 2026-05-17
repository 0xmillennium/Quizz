package com.quizz.quiz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quizz.common.exception.NotFoundException;
import com.quizz.question.entity.Question;
import com.quizz.quiz.entity.Quiz;
import com.quizz.quiz.entity.QuizQuestion;
import com.quizz.quiz.entity.QuizStatus;
import com.quizz.quiz.repository.QuizRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultQuizQueryServiceTest {

    @Mock
    private QuizRepository quizRepository;

    private Quiz quiz;
    private DefaultQuizQueryService service;

    @BeforeEach
    void setUp() {
        quiz = org.mockito.Mockito.mock(Quiz.class);
        service = new DefaultQuizQueryService(quizRepository);
    }

    @Test
    void getByIdReturnsQuiz() {
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));

        assertThat(service.getById(1L)).isSameAs(quiz);
    }

    @Test
    void getByIdMissingThrowsNotFoundException() {
        when(quizRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Quiz not found.");
    }

    @Test
    void getByIdWithAdminDetailsReturnsQuiz() {
        when(quizRepository.findByIdWithAdminDetails(1L)).thenReturn(Optional.of(quiz));

        assertThat(service.getByIdWithAdminDetails(1L)).isSameAs(quiz);
    }

    @Test
    void getByIdWithAdminDetailsMissingThrowsNotFoundException() {
        when(quizRepository.findByIdWithAdminDetails(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByIdWithAdminDetails(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Quiz not found.");
    }

    @Test
    void getPublishedByIdReturnsPublishedQuizDetails() {
        when(quizRepository.findByIdAndStatusWithDetails(1L, QuizStatus.PUBLISHED)).thenReturn(Optional.of(quiz));

        assertThat(service.getPublishedById(1L)).isSameAs(quiz);
    }

    @Test
    void getPublishedByIdMissingThrowsNotFoundException() {
        when(quizRepository.findByIdAndStatusWithDetails(1L, QuizStatus.PUBLISHED)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPublishedById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Published quiz not found.");
    }

    @Test
    void getPublishedByIdForAttemptReturnsQuizWithAttemptGraph() {
        Question question = mock(Question.class);
        QuizQuestion quizQuestion = mock(QuizQuestion.class);
        when(quizQuestion.getQuestion()).thenReturn(question);
        when(question.getId()).thenReturn(5L);
        when(quiz.getQuestions()).thenReturn(List.of(quizQuestion));
        when(quizRepository.findByIdAndStatusWithAttemptGraph(1L, QuizStatus.PUBLISHED)).thenReturn(Optional.of(quiz));

        assertThat(service.getPublishedByIdForAttempt(1L)).isSameAs(quiz);
        verify(quizRepository).findQuestionsWithOptionsByIdIn(List.of(5L));
    }

    @Test
    void getPublishedByIdForAttemptMissingThrowsNotFoundException() {
        when(quizRepository.findByIdAndStatusWithAttemptGraph(1L, QuizStatus.PUBLISHED)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPublishedByIdForAttempt(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Published quiz not found.");
    }

    @Test
    void findAllForAdminDelegates() {
        when(quizRepository.findAllWithCategoryAndQuestionsForAdmin()).thenReturn(List.of(quiz));

        assertThat(service.findAllForAdmin()).containsExactly(quiz);
    }

    @Test
    void findPublishedDelegatesPublishedQuery() {
        when(quizRepository.findByStatusWithCategoryAndQuestions(QuizStatus.PUBLISHED)).thenReturn(List.of(quiz));

        assertThat(service.findPublished()).containsExactly(quiz);
    }

    @Test
    void findPublishedByCategoryDelegatesCategoryAndPublishedQuery() {
        when(quizRepository.findByCategoryIdAndStatusWithCategoryAndQuestions(2L, QuizStatus.PUBLISHED))
                .thenReturn(List.of(quiz));

        assertThat(service.findPublishedByCategory(2L)).containsExactly(quiz);
        verify(quizRepository).findByCategoryIdAndStatusWithCategoryAndQuestions(2L, QuizStatus.PUBLISHED);
    }
}
