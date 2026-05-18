package com.quizz.quiz.mapper;

import com.quizz.quiz.dto.QuizAdminResponse;
import com.quizz.quiz.dto.QuizDetailResponse;
import com.quizz.quiz.dto.QuizQuestionResponse;
import com.quizz.quiz.dto.QuizSummaryResponse;
import com.quizz.quiz.dto.QuizUpdateRequest;
import com.quizz.quiz.entity.Quiz;
import com.quizz.quiz.entity.QuizQuestion;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class QuizMapper {

    public QuizAdminResponse toAdminResponse(Quiz quiz) {
        return new QuizAdminResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getCategory().getId(),
                quiz.getCategory().getName(),
                quiz.getDurationMinutes(),
                quiz.getQuestionCount(),
                quiz.getAttemptLimit(),
                quiz.getRetakeCooldownMinutes(),
                quiz.getStatus().name(),
                quiz.getQuestions().size(),
                quiz.getQuestions().stream()
                        .sorted(Comparator.comparingInt(QuizQuestion::getDisplayOrder))
                        .map(this::toQuizQuestionResponse)
                        .toList()
        );
    }

    public List<QuizAdminResponse> toAdminResponseList(List<Quiz> quizzes) {
        return quizzes.stream()
                .map(this::toAdminResponse)
                .toList();
    }

    public QuizSummaryResponse toSummaryResponse(Quiz quiz) {
        return new QuizSummaryResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getCategory().getName(),
                quiz.getDurationMinutes(),
                quiz.getQuestionCount(),
                quiz.getAttemptLimit(),
                quiz.getRetakeCooldownMinutes()
        );
    }

    public List<QuizSummaryResponse> toSummaryResponseList(List<Quiz> quizzes) {
        return quizzes.stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    public QuizDetailResponse toDetailResponse(Quiz quiz) {
        return new QuizDetailResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getCategory().getName(),
                quiz.getDurationMinutes(),
                quiz.getQuestionCount(),
                quiz.getAttemptLimit(),
                quiz.getRetakeCooldownMinutes(),
                quiz.getQuestions().size(),
                quiz.getQuestions().stream()
                        .sorted(Comparator.comparingInt(QuizQuestion::getDisplayOrder))
                        .map(this::toQuizQuestionResponse)
                        .toList()
        );
    }

    public QuizUpdateRequest toUpdateRequest(Quiz quiz) {
        QuizUpdateRequest request = new QuizUpdateRequest();
        request.setTitle(quiz.getTitle());
        request.setDescription(quiz.getDescription());
        request.setCategoryId(quiz.getCategory().getId());
        request.setDurationMinutes(quiz.getDurationMinutes());
        request.setQuestionCount(quiz.getQuestionCount());
        request.setAttemptLimit(quiz.getAttemptLimit());
        request.setRetakeCooldownMinutes(quiz.getRetakeCooldownMinutes());
        request.setQuestionIds(quiz.getQuestions().stream()
                .sorted(Comparator.comparingInt(QuizQuestion::getDisplayOrder))
                .map(quizQuestion -> quizQuestion.getQuestion().getId())
                .toList());
        return request;
    }

    private QuizQuestionResponse toQuizQuestionResponse(QuizQuestion quizQuestion) {
        return new QuizQuestionResponse(
                quizQuestion.getQuestion().getId(),
                quizQuestion.getQuestion().getText(),
                quizQuestion.getQuestion().getCategory().getName(),
                quizQuestion.getDisplayOrder()
        );
    }
}
