package com.quizz.quiz.service;

import com.quizz.category.entity.Category;
import com.quizz.category.service.CategoryQueryService;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.common.exception.NotFoundException;
import com.quizz.question.entity.Question;
import com.quizz.question.service.QuestionQueryService;
import com.quizz.quiz.dto.QuizCreateRequest;
import com.quizz.quiz.dto.QuizUpdateRequest;
import com.quizz.quiz.entity.Quiz;
import com.quizz.quiz.repository.QuizRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DefaultQuizCommandService implements QuizCommandService {

    private static final int MIN_DURATION_MINUTES = 1;
    private static final int MAX_DURATION_MINUTES = 180;

    private final QuizRepository quizRepository;
    private final CategoryQueryService categoryQueryService;
    private final QuestionQueryService questionQueryService;

    public DefaultQuizCommandService(
            QuizRepository quizRepository,
            CategoryQueryService categoryQueryService,
            QuestionQueryService questionQueryService) {
        this.quizRepository = quizRepository;
        this.categoryQueryService = categoryQueryService;
        this.questionQueryService = questionQueryService;
    }

    @Override
    public Quiz create(QuizCreateRequest request) {
        if (request == null) {
            throw new BusinessRuleException("Quiz request is required.");
        }

        String title = normalizeTitle(request.getTitle());
        String description = normalizeDescription(request.getDescription());
        validateRequest(
                title,
                description,
                request.getDurationMinutes(),
                request.getQuestionCount(),
                request.getAttemptLimit(),
                request.getRetakeCooldownMinutes(),
                request.getQuestionIds());
        Category category = categoryQueryService.getActiveById(request.getCategoryId());
        List<Question> questions = loadActiveQuestions(request.getQuestionIds());
        validateQuestionCategories(category, questions);

        return quizRepository.save(Quiz.create(
                title,
                description,
                category,
                request.getDurationMinutes(),
                request.getQuestionCount(),
                request.getAttemptLimit(),
                request.getRetakeCooldownMinutes(),
                questions));
    }

    @Override
    public Quiz updateDraft(Long quizId, QuizUpdateRequest request) {
        if (request == null) {
            throw new BusinessRuleException("Quiz request is required.");
        }

        Quiz quiz = quizRepository.findByIdWithAdminDetails(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz not found."));
        String title = normalizeTitle(request.getTitle());
        String description = normalizeDescription(request.getDescription());
        validateRequest(
                title,
                description,
                request.getDurationMinutes(),
                request.getQuestionCount(),
                request.getAttemptLimit(),
                request.getRetakeCooldownMinutes(),
                request.getQuestionIds());
        Category category = categoryQueryService.getActiveById(request.getCategoryId());
        List<Question> questions = loadActiveQuestions(request.getQuestionIds());
        validateQuestionCategories(category, questions);

        quiz.updateDraft(
                title,
                description,
                category,
                request.getDurationMinutes(),
                request.getQuestionCount(),
                request.getAttemptLimit(),
                request.getRetakeCooldownMinutes(),
                questions);
        return quiz;
    }

    @Override
    public void publish(Long quizId) {
        Quiz quiz = quizRepository.findByIdWithAdminDetails(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz not found."));

        if (!quiz.isDraft()) {
            quiz.publish();
            return;
        }
        if (quiz.getQuestions().isEmpty()) {
            throw new BusinessRuleException("Quiz must have at least one question.");
        }
        if (quiz.getQuestionCount() > quiz.getQuestions().size()) {
            throw new BusinessRuleException("Questions per attempt cannot exceed the pool size.");
        }
        if (!quiz.getCategory().isActive()) {
            throw new BusinessRuleException("Quiz category is inactive.");
        }
        if (quiz.getQuestions().stream().anyMatch(quizQuestion -> !quizQuestion.getQuestion().isActive())) {
            throw new BusinessRuleException("Quiz contains archived questions.");
        }
        quizRepository.findQuestionsWithOptionsByIdIn(quiz.getQuestions().stream()
                .map(quizQuestion -> quizQuestion.getQuestion().getId())
                .toList());
        if (quiz.getQuestions().stream().anyMatch(quizQuestion -> quizQuestion.getQuestion().getOptions().size() < 2)) {
            throw new BusinessRuleException("Every question must have at least two options.");
        }
        if (quiz.getQuestions().stream()
                .anyMatch(quizQuestion -> quizQuestion.getQuestion().getOptions().stream()
                        .filter(option -> option.isCorrect())
                        .count() != 1)) {
            throw new BusinessRuleException("Every question must have exactly one correct option.");
        }

        quiz.publish();
    }

    @Override
    public void archive(Long quizId) {
        quizRepository.findById(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz not found."))
                .archive();
    }

    private void validateRequest(
            String title,
            String description,
            Integer durationMinutes,
            Integer questionCount,
            Integer attemptLimit,
            Integer retakeCooldownMinutes,
            List<Long> questionIds) {
        if (title.isBlank()) {
            throw new BusinessRuleException("Quiz title is required.");
        }
        if (title.length() > 150) {
            throw new BusinessRuleException("Quiz title must not exceed 150 characters.");
        }
        if (description != null && description.length() > 1000) {
            throw new BusinessRuleException("Quiz description must not exceed 1000 characters.");
        }
        if (durationMinutes == null
                || durationMinutes < MIN_DURATION_MINUTES
                || durationMinutes > MAX_DURATION_MINUTES) {
            throw new BusinessRuleException("Quiz duration must be between 1 and 180 minutes.");
        }
        if (questionCount == null || questionCount < 1) {
            throw new BusinessRuleException("Questions per attempt must be at least 1.");
        }
        if (attemptLimit == null || attemptLimit < 1) {
            throw new BusinessRuleException("Attempt limit must be at least 1.");
        }
        if (retakeCooldownMinutes == null || retakeCooldownMinutes < 1) {
            throw new BusinessRuleException("Retake cooldown must be at least 1 minute.");
        }
        validateQuestionIds(questionIds);
        if (questionCount > questionIds.size()) {
            throw new BusinessRuleException("Questions per attempt cannot exceed the pool size.");
        }
    }

    private void validateQuestionIds(List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            throw new BusinessRuleException("Quiz must have at least one question.");
        }

        Set<Long> uniqueIds = new HashSet<>();
        for (Long questionId : questionIds) {
            if (questionId == null) {
                throw new BusinessRuleException("Selected questions must be active.");
            }
            if (!uniqueIds.add(questionId)) {
                throw new BusinessRuleException("Duplicate questions are not allowed.");
            }
        }
    }

    private List<Question> loadActiveQuestions(List<Long> questionIds) {
        List<Question> questions = new ArrayList<>();
        for (Long questionId : questionIds) {
            questions.add(questionQueryService.getActiveById(questionId));
        }
        return questions;
    }

    private void validateQuestionCategories(Category category, List<Question> questions) {
        for (Question question : questions) {
            if (!question.getCategory().getId().equals(category.getId())) {
                throw new BusinessRuleException("All selected questions must belong to the selected category.");
            }
        }
    }

    private String normalizeTitle(String title) {
        if (title == null) {
            return "";
        }
        return title.trim().replaceAll("\\s+", " ");
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }

        String trimmed = description.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        return trimmed;
    }
}
