package com.quizz.attempt.mapper;

import com.quizz.attempt.dto.AttemptAnswerOptionResponse;
import com.quizz.attempt.dto.AttemptQuestionResponse;
import com.quizz.attempt.dto.QuizAttemptPageResponse;
import com.quizz.attempt.dto.QuizHistoryResponse;
import com.quizz.attempt.dto.QuizResultResponse;
import com.quizz.attempt.dto.ResultAnswerOptionResponse;
import com.quizz.attempt.dto.ResultChartResponse;
import com.quizz.attempt.dto.ResultQuestionResponse;
import com.quizz.attempt.dto.SubmitQuizRequest;
import com.quizz.attempt.dto.UserAnswerRequest;
import com.quizz.attempt.entity.AttemptAnswerOption;
import com.quizz.attempt.entity.AttemptQuestion;
import com.quizz.attempt.entity.QuizAttempt;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class QuizAttemptMapper {

    public QuizAttemptPageResponse toAttemptPageResponse(QuizAttempt attempt) {
        return new QuizAttemptPageResponse(
                attempt.getId(),
                attempt.getQuizTitleSnapshot(),
                attempt.getCategoryNameSnapshot(),
                attempt.getDurationMinutes(),
                attempt.getStartedAt(),
                attempt.getExpiresAt(),
                attempt.getQuestions().stream()
                        .sorted(Comparator.comparingInt(AttemptQuestion::getDisplayOrder))
                        .map(this::toAttemptQuestionResponse)
                        .toList()
        );
    }

    public SubmitQuizRequest toSubmitQuizRequest(QuizAttempt attempt) {
        SubmitQuizRequest request = new SubmitQuizRequest();
        request.setAnswers(attempt.getQuestions().stream()
                .sorted(Comparator.comparingInt(AttemptQuestion::getDisplayOrder))
                .map(question -> {
                    UserAnswerRequest answer = new UserAnswerRequest();
                    answer.setAttemptQuestionId(question.getId());
                    answer.setSelectedOptionId(null);
                    return answer;
                })
                .toList());
        return request;
    }

    public QuizResultResponse toResultResponse(QuizAttempt attempt) {
        return new QuizResultResponse(
                attempt.getId(),
                attempt.getQuizTitleSnapshot(),
                attempt.getCategoryNameSnapshot(),
                attempt.getStatus().name(),
                attempt.getTotalQuestions(),
                attempt.getCorrectCount(),
                attempt.getWrongCount(),
                attempt.getUnansweredCount(),
                attempt.getScorePercentage(),
                attempt.getScoringVersion(),
                attempt.getStartedAt(),
                attempt.getExpiresAt(),
                attempt.getSubmittedAt(),
                attempt.getQuestions().stream()
                        .sorted(Comparator.comparingInt(AttemptQuestion::getDisplayOrder))
                        .map(this::toResultQuestionResponse)
                        .toList()
        );
    }

    public List<QuizHistoryResponse> toHistoryResponseList(List<QuizAttempt> attempts) {
        return attempts.stream()
                .map(attempt -> new QuizHistoryResponse(
                        attempt.getId(),
                        attempt.getQuizTitleSnapshot(),
                        attempt.getCategoryNameSnapshot(),
                        attempt.getStatus().name(),
                        attempt.getTotalQuestions(),
                        attempt.getCorrectCount(),
                        attempt.getScorePercentage(),
                        attempt.getStartedAt(),
                        attempt.getSubmittedAt()
                ))
                .toList();
    }

    public ResultChartResponse toChartResponse(QuizAttempt attempt) {
        return new ResultChartResponse(
                attempt.getCorrectCount(),
                attempt.getWrongCount(),
                attempt.getUnansweredCount()
        );
    }

    private AttemptQuestionResponse toAttemptQuestionResponse(AttemptQuestion question) {
        return new AttemptQuestionResponse(
                question.getId(),
                question.getQuestionText(),
                question.getDisplayOrder(),
                question.getOptions().stream()
                        .sorted(Comparator.comparingInt(AttemptAnswerOption::getDisplayOrder))
                        .map(option -> new AttemptAnswerOptionResponse(
                                option.getId(),
                                option.getOptionText(),
                                option.getDisplayOrder()
                        ))
                        .toList()
        );
    }

    private ResultQuestionResponse toResultQuestionResponse(AttemptQuestion question) {
        return new ResultQuestionResponse(
                question.getId(),
                question.getQuestionText(),
                question.getDisplayOrder(),
                question.getSelectedOptionId(),
                question.isAnswered(),
                question.isCorrectlyAnswered(),
                question.getOptions().stream()
                        .sorted(Comparator.comparingInt(AttemptAnswerOption::getDisplayOrder))
                        .map(option -> new ResultAnswerOptionResponse(
                                option.getId(),
                                option.getOptionText(),
                                option.isCorrect(),
                                option.getId().equals(question.getSelectedOptionId()),
                                option.getDisplayOrder()
                        ))
                        .toList()
        );
    }
}
