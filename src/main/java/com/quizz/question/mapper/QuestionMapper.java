package com.quizz.question.mapper;

import com.quizz.question.dto.AnswerOptionRequest;
import com.quizz.question.dto.AnswerOptionResponse;
import com.quizz.question.dto.QuestionResponse;
import com.quizz.question.dto.QuestionSelectionResponse;
import com.quizz.question.dto.QuestionSummaryResponse;
import com.quizz.question.dto.QuestionUpdateRequest;
import com.quizz.question.entity.AnswerOption;
import com.quizz.question.entity.Question;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class QuestionMapper {

    public QuestionResponse toResponse(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getText(),
                question.getCategory().getId(),
                question.getCategory().getName(),
                question.getStatus().name(),
                question.getOptions().stream()
                        .sorted(Comparator.comparingInt(AnswerOption::getDisplayOrder))
                        .map(this::toAnswerOptionResponse)
                        .toList()
        );
    }

    public QuestionSummaryResponse toSummaryResponse(Question question) {
        return new QuestionSummaryResponse(
                question.getId(),
                question.getText(),
                question.getCategory().getName(),
                question.getStatus().name(),
                question.getOptions().size()
        );
    }

    public List<QuestionSummaryResponse> toSummaryResponseList(List<Question> questions) {
        return questions.stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    public QuestionSelectionResponse toSelectionResponse(Question question) {
        return new QuestionSelectionResponse(
                question.getId(),
                question.getText(),
                question.getCategory().getId(),
                question.getCategory().getName()
        );
    }

    public List<QuestionSelectionResponse> toSelectionResponseList(List<Question> questions) {
        return questions.stream()
                .map(this::toSelectionResponse)
                .toList();
    }

    public QuestionUpdateRequest toUpdateRequest(Question question) {
        QuestionUpdateRequest request = new QuestionUpdateRequest();
        request.setText(question.getText());
        request.setCategoryId(question.getCategory().getId());
        request.setOptions(question.getOptions().stream()
                .sorted(Comparator.comparingInt(AnswerOption::getDisplayOrder))
                .map(this::toAnswerOptionRequest)
                .toList());
        return request;
    }

    private AnswerOptionResponse toAnswerOptionResponse(AnswerOption option) {
        return new AnswerOptionResponse(
                option.getId(),
                option.getText(),
                option.isCorrect(),
                option.getDisplayOrder()
        );
    }

    private AnswerOptionRequest toAnswerOptionRequest(AnswerOption option) {
        AnswerOptionRequest request = new AnswerOptionRequest();
        request.setText(option.getText());
        request.setCorrect(option.isCorrect());
        return request;
    }
}
