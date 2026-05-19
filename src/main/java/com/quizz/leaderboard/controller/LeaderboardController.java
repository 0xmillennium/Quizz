package com.quizz.leaderboard.controller;

import com.quizz.category.mapper.CategoryMapper;
import com.quizz.category.service.CategoryQueryService;
import com.quizz.leaderboard.dto.LeaderboardFilterRequest;
import com.quizz.leaderboard.service.LeaderboardService;
import com.quizz.quiz.mapper.QuizMapper;
import com.quizz.quiz.service.QuizQueryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Authenticated MVC boundary for public leaderboard views.
 *
 * <p>The controller binds leaderboard filters and prepares selector data while
 * delegating ranking semantics to {@link LeaderboardService}. It does not query
 * attempt tables directly or expose user email addresses.</p>
 */
@Controller
@RequestMapping("/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;
    private final CategoryQueryService categoryQueryService;
    private final CategoryMapper categoryMapper;
    private final QuizQueryService quizQueryService;
    private final QuizMapper quizMapper;

    public LeaderboardController(
            LeaderboardService leaderboardService,
            CategoryQueryService categoryQueryService,
            CategoryMapper categoryMapper,
            QuizQueryService quizQueryService,
            QuizMapper quizMapper
    ) {
        this.leaderboardService = leaderboardService;
        this.categoryQueryService = categoryQueryService;
        this.categoryMapper = categoryMapper;
        this.quizQueryService = quizQueryService;
        this.quizMapper = quizMapper;
    }

    @GetMapping
    public String index(@ModelAttribute LeaderboardFilterRequest filter, Model model) {
        model.addAttribute("leaderboard", leaderboardService.getLeaderboard(filter));
        model.addAttribute("filter", filter);
        model.addAttribute("categories", categoryMapper.toOptionResponseList(categoryQueryService.findActive()));
        model.addAttribute("quizzes", quizMapper.toSummaryResponseList(quizQueryService.findPublished()));
        return "leaderboard/index";
    }
}
