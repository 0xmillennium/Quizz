package com.quizz.question.service;

import com.quizz.category.entity.Category;
import com.quizz.category.service.CategoryQueryService;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.common.exception.NotFoundException;
import com.quizz.question.dto.AnswerOptionRequest;
import com.quizz.question.dto.QuestionCreateRequest;
import com.quizz.question.dto.QuestionUpdateRequest;
import com.quizz.question.entity.AnswerOptionDraft;
import com.quizz.question.entity.Question;
import com.quizz.question.repository.QuestionRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DefaultQuestionCommandService implements QuestionCommandService {

    private static final int MIN_OPTIONS = 2;
    private static final int MAX_OPTIONS = 6;

    private final QuestionRepository questionRepository;
    private final CategoryQueryService categoryQueryService;

    public DefaultQuestionCommandService(
            QuestionRepository questionRepository,
            CategoryQueryService categoryQueryService
    ) {
        this.questionRepository = questionRepository;
        this.categoryQueryService = categoryQueryService;
    }

    @Override
    public Question create(QuestionCreateRequest request) {
        if (request == null) {
            throw new BusinessRuleException("Question request is required.");
        }

        String text = normalizeQuestionText(request.getText());
        Category category = categoryQueryService.getActiveById(request.getCategoryId());
        List<AnswerOptionDraft> drafts = normalizeAndValidateOptions(request.getOptions());
        validateQuestionText(text);

        return questionRepository.save(Question.create(text, category, drafts));
    }

    @Override
    public Question update(Long questionId, QuestionUpdateRequest request) {
        if (request == null) {
            throw new BusinessRuleException("Question request is required.");
        }

        Question question = questionRepository.findByIdWithDetails(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found."));
        String text = normalizeQuestionText(request.getText());
        Category category = categoryQueryService.getActiveById(request.getCategoryId());
        List<AnswerOptionDraft> drafts = normalizeAndValidateOptions(request.getOptions());
        validateQuestionText(text);

        question.update(text, category, drafts);
        return question;
    }

    @Override
    public void archive(Long questionId) {
        questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found."))
                .archive();
    }

    @Override
    public void restore(Long questionId) {
        questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found."))
                .restore();
    }

    private void validateQuestionText(String text) {
        if (text.isBlank()) {
            throw new BusinessRuleException("Question text is required.");
        }
        if (text.length() > 1000) {
            throw new BusinessRuleException("Question text must not exceed 1000 characters.");
        }
    }

    private List<AnswerOptionDraft> normalizeAndValidateOptions(List<AnswerOptionRequest> options) {
        if (options == null || options.size() < MIN_OPTIONS) {
            throw new BusinessRuleException("A question must have at least 2 options.");
        }
        if (options.size() > MAX_OPTIONS) {
            throw new BusinessRuleException("A question can have at most 6 options.");
        }

        List<AnswerOptionDraft> drafts = new ArrayList<>();
        Set<String> uniqueTexts = new HashSet<>();
        int correctCount = 0;

        for (AnswerOptionRequest option : options) {
            String optionText = option == null ? "" : normalizeOptionText(option.getText());
            boolean correct = option != null && option.isCorrect();

            if (optionText.isBlank()) {
                throw new BusinessRuleException("Option text is required.");
            }
            if (optionText.length() > 500) {
                throw new BusinessRuleException("Option text must not exceed 500 characters.");
            }
            if (!uniqueTexts.add(optionText.toLowerCase(Locale.ROOT))) {
                throw new BusinessRuleException("Option text must be unique.");
            }
            if (correct) {
                correctCount++;
            }

            drafts.add(new AnswerOptionDraft(optionText, correct));
        }

        if (correctCount != 1) {
            throw new BusinessRuleException("Exactly one option must be marked as correct.");
        }

        return drafts;
    }

    private String normalizeQuestionText(String text) {
        if (text == null) {
            return "";
        }
        return text.trim();
    }

    private String normalizeOptionText(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().replaceAll("\\s+", " ");
    }
}
