package com.quizz.question.validation;

import com.quizz.category.service.CategoryQueryService;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.common.exception.NotFoundException;
import com.quizz.question.dto.AnswerOptionRequest;
import com.quizz.question.dto.QuestionCreateRequest;
import com.quizz.question.dto.QuestionUpdateRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class QuestionFormValidator {

    private static final int MIN_OPTIONS = 2;
    private static final int MAX_OPTIONS = 6;

    private final CategoryQueryService categoryQueryService;

    public QuestionFormValidator(CategoryQueryService categoryQueryService) {
        this.categoryQueryService = categoryQueryService;
    }

    public void validateCreate(QuestionCreateRequest request, BindingResult bindingResult) {
        validateCategory(request.getCategoryId(), bindingResult);
        validateOptions(request.getOptions(), bindingResult);
    }

    public void validateUpdate(QuestionUpdateRequest request, BindingResult bindingResult) {
        validateCategory(request.getCategoryId(), bindingResult);
        validateOptions(request.getOptions(), bindingResult);
    }

    private void validateCategory(Long categoryId, BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors("categoryId")) {
            return;
        }
        if (categoryId == null) {
            return;
        }

        try {
            categoryQueryService.getActiveById(categoryId);
        } catch (NotFoundException | BusinessRuleException ex) {
            bindingResult.rejectValue(
                    "categoryId",
                    "question.category.invalid",
                    "Please select an active category.");
        }
    }

    private void validateOptions(List<AnswerOptionRequest> options, BindingResult bindingResult) {
        if (options == null || options.size() < MIN_OPTIONS) {
            bindingResult.rejectValue(
                    "options",
                    "question.options.count",
                    "A question must have at least 2 options.");
        } else if (options.size() > MAX_OPTIONS) {
            bindingResult.rejectValue(
                    "options",
                    "question.options.count",
                    "A question can have at most 6 options.");
        }

        if (options == null) {
            return;
        }

        int correctCount = 0;
        Set<String> uniqueTexts = new HashSet<>();
        for (int i = 0; i < options.size(); i++) {
            AnswerOptionRequest option = options.get(i);
            String text = option == null ? "" : normalizeOptionText(option.getText());
            if (option != null && option.isCorrect()) {
                correctCount++;
            }

            if (text.isBlank()) {
                bindingResult.rejectValue(
                        "options[" + i + "].text",
                        "question.options.textRequired",
                        "Option text is required.");
                continue;
            }

            if (!uniqueTexts.add(text.toLowerCase(Locale.ROOT))) {
                bindingResult.rejectValue(
                        "options[" + i + "].text",
                        "question.options.duplicate",
                        "Option text must be unique.");
            }
        }

        if (correctCount != 1) {
            bindingResult.rejectValue(
                    "options",
                    "question.options.correctRequired",
                    "Exactly one option must be marked as correct.");
        }
    }

    private String normalizeOptionText(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().replaceAll("\\s+", " ");
    }
}
