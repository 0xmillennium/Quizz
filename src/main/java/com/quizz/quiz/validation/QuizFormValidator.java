package com.quizz.quiz.validation;

import com.quizz.category.entity.Category;
import com.quizz.category.service.CategoryQueryService;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.common.exception.NotFoundException;
import com.quizz.question.entity.Question;
import com.quizz.question.service.QuestionQueryService;
import com.quizz.quiz.dto.QuizCreateRequest;
import com.quizz.quiz.dto.QuizUpdateRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class QuizFormValidator {

    private final CategoryQueryService categoryQueryService;
    private final QuestionQueryService questionQueryService;

    public QuizFormValidator(
            CategoryQueryService categoryQueryService,
            QuestionQueryService questionQueryService) {
        this.categoryQueryService = categoryQueryService;
        this.questionQueryService = questionQueryService;
    }

    public void validateCreate(QuizCreateRequest request, BindingResult bindingResult) {
        validate(
                request.getCategoryId(),
                request.getDurationMinutes(),
                request.getQuestionCount(),
                request.getAttemptLimit(),
                request.getRetakeCooldownMinutes(),
                request.getQuestionIds(),
                bindingResult);
    }

    public void validateUpdate(QuizUpdateRequest request, BindingResult bindingResult) {
        validate(
                request.getCategoryId(),
                request.getDurationMinutes(),
                request.getQuestionCount(),
                request.getAttemptLimit(),
                request.getRetakeCooldownMinutes(),
                request.getQuestionIds(),
                bindingResult);
    }

    private void validate(
            Long categoryId,
            Integer durationMinutes,
            Integer questionCount,
            Integer attemptLimit,
            Integer retakeCooldownMinutes,
            List<Long> questionIds,
            BindingResult bindingResult) {
        Category category = validateCategory(categoryId, bindingResult);
        validateDuration(durationMinutes, bindingResult);
        validatePositive("questionCount", questionCount, "Questions per attempt must be at least 1.", bindingResult);
        validatePositive("attemptLimit", attemptLimit, "Attempt limit must be at least 1.", bindingResult);
        validatePositive(
                "retakeCooldownMinutes",
                retakeCooldownMinutes,
                "Retake cooldown must be at least 1 minute.",
                bindingResult);
        List<Question> questions = validateQuestions(questionIds, bindingResult);
        validateQuestionCount(questionCount, questions, bindingResult);
        validateQuestionCategories(category, questions, bindingResult);
    }

    private Category validateCategory(Long categoryId, BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors("categoryId") || categoryId == null) {
            return null;
        }

        try {
            return categoryQueryService.getActiveById(categoryId);
        } catch (NotFoundException | BusinessRuleException ex) {
            bindingResult.rejectValue(
                    "categoryId",
                    "quiz.category.invalid",
                    "Please select an active category.");
            return null;
        }
    }

    private void validatePositive(
            String field,
            Integer value,
            String message,
            BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors(field) || value == null) {
            return;
        }
        if (value < 1) {
            bindingResult.rejectValue(field, "quiz." + field + ".min", message);
        }
    }

    private void validateDuration(Integer durationMinutes, BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors("durationMinutes") || durationMinutes == null) {
            return;
        }
        if (durationMinutes < 1 || durationMinutes > 180) {
            bindingResult.rejectValue(
                    "durationMinutes",
                    "quiz.duration.range",
                    "Duration must be between 1 and 180 minutes.");
        }
    }

    private void validateQuestionCount(
            Integer questionCount,
            List<Question> questions,
            BindingResult bindingResult) {
        if (questionCount == null
                || questions.isEmpty()
                || bindingResult.hasFieldErrors("questionCount")
                || bindingResult.hasFieldErrors("questionIds")) {
            return;
        }
        if (questionCount > questions.size()) {
            bindingResult.rejectValue(
                    "questionCount",
                    "quiz.questionCount.poolSize",
                    "Questions per attempt cannot exceed the selected pool size.");
        }
    }

    private List<Question> validateQuestions(List<Long> questionIds, BindingResult bindingResult) {
        if (questionIds == null || questionIds.isEmpty()) {
            bindingResult.rejectValue(
                    "questionIds",
                    "quiz.questions.required",
                    "Select at least one question.");
            return List.of();
        }

        if (questionIds.stream().anyMatch(questionId -> questionId == null)) {
            bindingResult.rejectValue(
                    "questionIds",
                    "quiz.questions.invalid",
                    "Selected questions must be active.");
            return List.of();
        }

        if (containsDuplicates(questionIds)) {
            bindingResult.rejectValue(
                    "questionIds",
                    "quiz.questions.duplicate",
                    "Duplicate questions are not allowed.");
            return List.of();
        }

        if (bindingResult.hasFieldErrors("questionIds")) {
            return List.of();
        }

        List<Question> questions = new ArrayList<>();
        for (Long questionId : questionIds) {
            try {
                questions.add(questionQueryService.getActiveById(questionId));
            } catch (NotFoundException | BusinessRuleException ex) {
                bindingResult.rejectValue(
                        "questionIds",
                        "quiz.questions.invalid",
                        "Selected questions must be active.");
                return List.of();
            }
        }
        return questions;
    }

    private void validateQuestionCategories(
            Category category,
            List<Question> questions,
            BindingResult bindingResult) {
        if (category == null || questions.isEmpty() || bindingResult.hasFieldErrors("questionIds")) {
            return;
        }

        for (Question question : questions) {
            if (!question.getCategory().getId().equals(category.getId())) {
                bindingResult.rejectValue(
                        "questionIds",
                        "quiz.questions.categoryMismatch",
                        "All selected questions must belong to the selected category.");
                return;
            }
        }
    }

    private boolean containsDuplicates(List<Long> questionIds) {
        Set<Long> uniqueIds = new HashSet<>();
        for (Long questionId : questionIds) {
            if (!uniqueIds.add(questionId)) {
                return true;
            }
        }
        return false;
    }
}
