package com.quizz.quiz.controller;

import com.quizz.category.mapper.CategoryMapper;
import com.quizz.category.service.CategoryQueryService;
import com.quizz.quiz.mapper.QuizMapper;
import com.quizz.quiz.service.QuizQueryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class QuizController {

    private final QuizQueryService quizQueryService;
    private final QuizMapper quizMapper;
    private final CategoryQueryService categoryQueryService;
    private final CategoryMapper categoryMapper;

    public QuizController(
            QuizQueryService quizQueryService,
            QuizMapper quizMapper,
            CategoryQueryService categoryQueryService,
            CategoryMapper categoryMapper
    ) {
        this.quizQueryService = quizQueryService;
        this.quizMapper = quizMapper;
        this.categoryQueryService = categoryQueryService;
        this.categoryMapper = categoryMapper;
    }

    @GetMapping("/quizzes")
    public String list(@RequestParam(required = false) Long categoryId, Model model) {
        model.addAttribute(
                "categories",
                categoryMapper.toOptionResponseList(categoryQueryService.findActive())
        );
        model.addAttribute(
                "quizzes",
                quizMapper.toSummaryResponseList(categoryId == null
                        ? quizQueryService.findPublished()
                        : quizQueryService.findPublishedByCategory(categoryId))
        );
        model.addAttribute("selectedCategoryId", categoryId);
        return "quiz/list";
    }

    @GetMapping("/quizzes/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("quiz", quizMapper.toDetailResponse(quizQueryService.getPublishedById(id)));
        return "quiz/detail";
    }
}
