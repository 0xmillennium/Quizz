package com.quizz.admin.controller;

import com.quizz.admin.dto.AdminResultFilterRequest;
import com.quizz.admin.dto.AdminResultListResponse;
import com.quizz.admin.service.AdminResultService;
import com.quizz.category.mapper.CategoryMapper;
import com.quizz.category.service.CategoryQueryService;
import com.quizz.quiz.mapper.QuizMapper;
import com.quizz.quiz.service.QuizQueryService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * ADMIN MVC boundary for attempt-result reports.
 *
 * <p>The controller handles filter binding and view selection for
 * {@code /admin/results}. Reporting data comes from {@link AdminResultService}
 * snapshot read models; no repository access or score recalculation belongs in
 * this layer.</p>
 */
@Controller
@RequestMapping("/admin/results")
public class AdminResultController {

    private final AdminResultService adminResultService;
    private final CategoryQueryService categoryQueryService;
    private final CategoryMapper categoryMapper;
    private final QuizQueryService quizQueryService;
    private final QuizMapper quizMapper;

    public AdminResultController(
            AdminResultService adminResultService,
            CategoryQueryService categoryQueryService,
            CategoryMapper categoryMapper,
            QuizQueryService quizQueryService,
            QuizMapper quizMapper
    ) {
        this.adminResultService = adminResultService;
        this.categoryQueryService = categoryQueryService;
        this.categoryMapper = categoryMapper;
        this.quizQueryService = quizQueryService;
        this.quizMapper = quizMapper;
    }

    @GetMapping
    public String list(@ModelAttribute AdminResultFilterRequest filter, Model model) {
        AdminResultListResponse response = adminResultService.searchResults(filter);
        model.addAttribute("results", response);
        model.addAttribute("filter", response.filter());
        model.addAttribute("categories", categoryMapper.toOptionResponseList(categoryQueryService.findActive()));
        model.addAttribute("quizzes", quizMapper.toSummaryResponseList(quizQueryService.findPublished()));
        model.addAttribute("statuses", List.of("IN_PROGRESS", "COMPLETED", "ABANDONED"));
        return "admin/results/list";
    }

    @GetMapping("/{attemptId}")
    public String detail(@PathVariable Long attemptId, Model model) {
        model.addAttribute("result", adminResultService.getResultDetail(attemptId));
        return "admin/results/detail";
    }
}
