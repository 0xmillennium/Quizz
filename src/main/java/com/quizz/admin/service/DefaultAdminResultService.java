package com.quizz.admin.service;

import com.quizz.admin.dto.AdminPageResponse;
import com.quizz.admin.dto.AdminResultAnswerOptionResponse;
import com.quizz.admin.dto.AdminResultDetailResponse;
import com.quizz.admin.dto.AdminResultFilterRequest;
import com.quizz.admin.dto.AdminResultListResponse;
import com.quizz.admin.dto.AdminResultQuestionResponse;
import com.quizz.admin.dto.AdminResultSummaryResponse;
import com.quizz.admin.repository.AdminResultQueryRepository;
import com.quizz.admin.repository.AdminResultQueryRepository.AdminResultAttemptRow;
import com.quizz.admin.repository.AdminResultQueryRepository.AdminResultQuestionOptionRow;
import com.quizz.admin.repository.AdminResultSearchCriteria;
import com.quizz.attempt.entity.AttemptStatus;
import com.quizz.category.service.CategoryQueryService;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.common.exception.NotFoundException;
import com.quizz.quiz.service.QuizQueryService;
import com.quizz.user.service.UserQueryService;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DefaultAdminResultService implements AdminResultService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final AdminResultQueryRepository repository;
    private final UserQueryService userQueryService;
    private final QuizQueryService quizQueryService;
    private final CategoryQueryService categoryQueryService;

    public DefaultAdminResultService(
            AdminResultQueryRepository repository,
            UserQueryService userQueryService,
            QuizQueryService quizQueryService,
            CategoryQueryService categoryQueryService
    ) {
        this.repository = repository;
        this.userQueryService = userQueryService;
        this.quizQueryService = quizQueryService;
        this.categoryQueryService = categoryQueryService;
    }

    @Override
    public AdminResultListResponse searchResults(AdminResultFilterRequest filter) {
        AdminResultFilterRequest normalizedFilter = filter == null ? new AdminResultFilterRequest() : filter;
        int page = normalizePage(normalizedFilter.getPage());
        int size = normalizeSize(normalizedFilter.getSize());
        normalizedFilter.setPage(page);
        normalizedFilter.setSize(size);

        AttemptStatus status = parseStatus(normalizedFilter.getStatus());
        normalizedFilter.setStatus(status == null ? null : status.name());
        validateDateRange(normalizedFilter);

        Instant startedFrom = normalizedFilter.getStartedFrom() == null
                ? null
                : normalizedFilter.getStartedFrom().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant startedToExclusive = normalizedFilter.getStartedTo() == null
                ? null
                : normalizedFilter.getStartedTo().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        validateReferencedFilters(normalizedFilter);

        AdminResultSearchCriteria criteria = new AdminResultSearchCriteria(
                normalizedFilter.getUserId(),
                normalizedFilter.getQuizId(),
                normalizedFilter.getCategoryId(),
                status,
                startedFrom,
                startedToExclusive,
                size,
                (page - 1) * size
        );

        long total = repository.countResults(criteria);
        List<AdminResultSummaryResponse> results = repository.findResults(criteria).stream()
                .map(row -> new AdminResultSummaryResponse(
                        row.attemptId(),
                        row.userId(),
                        row.userFullName(),
                        row.quizTitle(),
                        row.categoryName(),
                        row.status(),
                        row.totalQuestions(),
                        row.correctCount(),
                        row.wrongCount(),
                        row.unansweredCount(),
                        row.scorePercentage(),
                        row.startedAt(),
                        row.expiresAt(),
                        row.submittedAt()
                ))
                .toList();

        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / size);
        AdminPageResponse pageResponse = new AdminPageResponse(
                page,
                size,
                total,
                totalPages,
                page > 1,
                totalPages > 0 && page < totalPages
        );

        return new AdminResultListResponse(normalizedFilter, pageResponse, results);
    }

    @Override
    public AdminResultDetailResponse getResultDetail(Long attemptId) {
        AdminResultAttemptRow header = repository.findAttemptHeader(attemptId)
                .orElseThrow(() -> new NotFoundException("Attempt not found."));
        List<AdminResultQuestionResponse> questions = groupQuestions(
                repository.findAttemptQuestionOptionRows(attemptId)
        );

        return new AdminResultDetailResponse(
                header.attemptId(),
                header.userId(),
                header.userFullName(),
                header.quizTitle(),
                header.quizId(),
                header.categoryId(),
                header.categoryName(),
                header.status(),
                header.totalQuestions(),
                header.correctCount(),
                header.wrongCount(),
                header.unansweredCount(),
                header.scorePercentage(),
                header.scoringVersion(),
                header.startedAt(),
                header.expiresAt(),
                header.submittedAt(),
                questions
        );
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? DEFAULT_PAGE : page;
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private AttemptStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        try {
            return AttemptStatus.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new BusinessRuleException("Invalid attempt status.");
        }
    }

    private void validateDateRange(AdminResultFilterRequest filter) {
        if (filter.getStartedFrom() != null
                && filter.getStartedTo() != null
                && filter.getStartedFrom().isAfter(filter.getStartedTo())) {
            throw new BusinessRuleException("Invalid date range.");
        }
    }

    private void validateReferencedFilters(AdminResultFilterRequest filter) {
        if (filter.getUserId() != null) {
            userQueryService.getById(filter.getUserId());
        }
        if (filter.getQuizId() != null) {
            quizQueryService.getById(filter.getQuizId());
        }
        if (filter.getCategoryId() != null) {
            categoryQueryService.getById(filter.getCategoryId());
        }
    }

    private List<AdminResultQuestionResponse> groupQuestions(List<AdminResultQuestionOptionRow> rows) {
        Map<Long, QuestionAccumulator> grouped = new LinkedHashMap<>();
        for (AdminResultQuestionOptionRow row : rows) {
            QuestionAccumulator question = grouped.computeIfAbsent(row.attemptQuestionId(), id -> new QuestionAccumulator(
                    row.attemptQuestionId(),
                    row.originalQuestionId(),
                    row.questionText(),
                    row.questionDisplayOrder(),
                    row.selectedOptionId(),
                    row.questionCorrect()
            ));
            question.options().add(new AdminResultAnswerOptionResponse(
                    row.attemptAnswerOptionId(),
                    row.originalAnswerOptionId(),
                    row.optionText(),
                    row.optionCorrect(),
                    Objects.equals(row.attemptAnswerOptionId(), question.selectedOptionId()),
                    row.optionDisplayOrder()
            ));
        }

        return grouped.values().stream()
                .map(question -> new AdminResultQuestionResponse(
                        question.attemptQuestionId(),
                        question.originalQuestionId(),
                        question.questionText(),
                        question.displayOrder(),
                        question.selectedOptionId(),
                        question.correct(),
                        List.copyOf(question.options())
                ))
                .toList();
    }

    private record QuestionAccumulator(
            Long attemptQuestionId,
            Long originalQuestionId,
            String questionText,
            int displayOrder,
            Long selectedOptionId,
            Boolean correct,
            List<AdminResultAnswerOptionResponse> options
    ) {

        private QuestionAccumulator(
                Long attemptQuestionId,
                Long originalQuestionId,
                String questionText,
                int displayOrder,
                Long selectedOptionId,
                Boolean correct
        ) {
            this(
                    attemptQuestionId,
                    originalQuestionId,
                    questionText,
                    displayOrder,
                    selectedOptionId,
                    correct,
                    new ArrayList<>()
            );
        }
    }
}
