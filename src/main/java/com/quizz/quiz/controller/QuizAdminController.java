package com.quizz.quiz.controller;

import com.quizz.category.mapper.CategoryMapper;
import com.quizz.category.service.CategoryQueryService;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.common.web.FlashMessage;
import com.quizz.question.mapper.QuestionMapper;
import com.quizz.question.service.QuestionQueryService;
import com.quizz.quiz.dto.QuizCreateRequest;
import com.quizz.quiz.dto.QuizUpdateRequest;
import com.quizz.quiz.entity.Quiz;
import com.quizz.quiz.mapper.QuizMapper;
import com.quizz.quiz.service.QuizCommandService;
import com.quizz.quiz.service.QuizQueryService;
import com.quizz.quiz.validation.QuizFormValidator;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * ADMIN MVC boundary for quiz definition authoring.
 *
 * <p>
 * The controller serves {@code /admin/quizzes} views, prepares category and
 * question selectors, and delegates draft, publish, and archive operations to
 * {@link QuizCommandService}. It keeps repository fetch and publication rules
 * behind service contracts.
 * </p>
 */
@Controller
@RequestMapping("/admin/quizzes")
public class QuizAdminController {

    private final QuizCommandService quizCommandService;
    private final QuizQueryService quizQueryService;
    private final QuizMapper quizMapper;
    private final QuizFormValidator quizFormValidator;
    private final CategoryQueryService categoryQueryService;
    private final CategoryMapper categoryMapper;
    private final QuestionQueryService questionQueryService;
    private final QuestionMapper questionMapper;

    public QuizAdminController(
            QuizCommandService quizCommandService,
            QuizQueryService quizQueryService,
            QuizMapper quizMapper,
            QuizFormValidator quizFormValidator,
            CategoryQueryService categoryQueryService,
            CategoryMapper categoryMapper,
            QuestionQueryService questionQueryService,
            QuestionMapper questionMapper) {
        this.quizCommandService = quizCommandService;
        this.quizQueryService = quizQueryService;
        this.quizMapper = quizMapper;
        this.quizFormValidator = quizFormValidator;
        this.categoryQueryService = categoryQueryService;
        this.categoryMapper = categoryMapper;
        this.questionQueryService = questionQueryService;
        this.questionMapper = questionMapper;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("quizzes", quizMapper.toAdminResponseList(quizQueryService.findAllForAdmin()));
        return "admin/quizzes/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("quizCreateRequest", new QuizCreateRequest());
        addFormOptions(model);
        return "admin/quizzes/create";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("quizCreateRequest") QuizCreateRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        quizFormValidator.validateCreate(request, bindingResult);
        if (bindingResult.hasErrors()) {
            addFormOptions(model);
            return "admin/quizzes/create";
        }

        quizCommandService.create(request);
        redirectAttributes.addFlashAttribute(
                "flashMessage",
                FlashMessage.success("Quiz draft created successfully."));
        return "redirect:/admin/quizzes";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("quiz", quizMapper.toAdminResponse(quizQueryService.getByIdWithAdminDetails(id)));
        return "admin/quizzes/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Quiz quiz = quizQueryService.getByIdWithAdminDetails(id);
        if (!quiz.isDraft()) {
            throw new BusinessRuleException("Only draft quizzes can be edited.");
        }

        model.addAttribute("quizId", id);
        model.addAttribute("quizUpdateRequest", quizMapper.toUpdateRequest(quiz));
        addFormOptions(model);
        return "admin/quizzes/edit";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("quizUpdateRequest") QuizUpdateRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        quizFormValidator.validateUpdate(request, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("quizId", id);
            addFormOptions(model);
            return "admin/quizzes/edit";
        }

        quizCommandService.updateDraft(id, request);
        redirectAttributes.addFlashAttribute(
                "flashMessage",
                FlashMessage.success("Quiz draft updated successfully."));
        return "redirect:/admin/quizzes/" + id;
    }

    @PostMapping("/{id}/publish")
    public String publish(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        quizCommandService.publish(id);
        redirectAttributes.addFlashAttribute(
                "flashMessage",
                FlashMessage.success("Quiz published successfully."));
        return "redirect:/admin/quizzes/" + id;
    }

    @PostMapping("/{id}/archive")
    public String archive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        quizCommandService.archive(id);
        redirectAttributes.addFlashAttribute(
                "flashMessage",
                FlashMessage.success("Quiz archived successfully."));
        return "redirect:/admin/quizzes";
    }

    private void addFormOptions(Model model) {
        model.addAttribute(
                "categories",
                categoryMapper.toOptionResponseList(categoryQueryService.findActive()));
        model.addAttribute(
                "questions",
                questionMapper.toSelectionResponseList(questionQueryService.findActive()));
    }
}
