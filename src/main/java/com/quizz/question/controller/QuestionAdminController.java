package com.quizz.question.controller;

import com.quizz.category.mapper.CategoryMapper;
import com.quizz.category.service.CategoryQueryService;
import com.quizz.common.web.FlashMessage;
import com.quizz.question.dto.AnswerOptionRequest;
import com.quizz.question.dto.QuestionCreateRequest;
import com.quizz.question.dto.QuestionUpdateRequest;
import com.quizz.question.entity.Question;
import com.quizz.question.mapper.QuestionMapper;
import com.quizz.question.service.QuestionCommandService;
import com.quizz.question.service.QuestionQueryService;
import com.quizz.question.validation.QuestionFormValidator;
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

@Controller
@RequestMapping("/admin/questions")
public class QuestionAdminController {

    private final QuestionCommandService questionCommandService;
    private final QuestionQueryService questionQueryService;
    private final QuestionMapper questionMapper;
    private final QuestionFormValidator questionFormValidator;
    private final CategoryQueryService categoryQueryService;
    private final CategoryMapper categoryMapper;

    public QuestionAdminController(
            QuestionCommandService questionCommandService,
            QuestionQueryService questionQueryService,
            QuestionMapper questionMapper,
            QuestionFormValidator questionFormValidator,
            CategoryQueryService categoryQueryService,
            CategoryMapper categoryMapper
    ) {
        this.questionCommandService = questionCommandService;
        this.questionQueryService = questionQueryService;
        this.questionMapper = questionMapper;
        this.questionFormValidator = questionFormValidator;
        this.categoryQueryService = categoryQueryService;
        this.categoryMapper = categoryMapper;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute(
                "questions",
                questionMapper.toSummaryResponseList(questionQueryService.findAllForAdmin())
        );
        return "admin/questions/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("questionCreateRequest", createRequestWithDefaultOptions());
        addActiveCategories(model);
        return "admin/questions/create";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("questionCreateRequest") QuestionCreateRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        questionFormValidator.validateCreate(request, bindingResult);
        if (bindingResult.hasErrors()) {
            addActiveCategories(model);
            return "admin/questions/create";
        }

        questionCommandService.create(request);
        redirectAttributes.addFlashAttribute(
                "flashMessage",
                FlashMessage.success("Question created successfully.")
        );
        return "redirect:/admin/questions";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Question question = questionQueryService.getByIdWithDetails(id);
        model.addAttribute("questionId", id);
        model.addAttribute("questionUpdateRequest", questionMapper.toUpdateRequest(question));
        addActiveCategories(model);
        return "admin/questions/edit";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("questionUpdateRequest") QuestionUpdateRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        questionFormValidator.validateUpdate(request, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("questionId", id);
            addActiveCategories(model);
            return "admin/questions/edit";
        }

        questionCommandService.update(id, request);
        redirectAttributes.addFlashAttribute(
                "flashMessage",
                FlashMessage.success("Question updated successfully.")
        );
        return "redirect:/admin/questions";
    }

    @PostMapping("/{id}/delete")
    public String archive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        questionCommandService.archive(id);
        redirectAttributes.addFlashAttribute(
                "flashMessage",
                FlashMessage.success("Question archived successfully.")
        );
        return "redirect:/admin/questions";
    }

    @PostMapping("/{id}/restore")
    public String restore(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        questionCommandService.restore(id);
        redirectAttributes.addFlashAttribute(
                "flashMessage",
                FlashMessage.success("Question restored successfully.")
        );
        return "redirect:/admin/questions";
    }

    private void addActiveCategories(Model model) {
        model.addAttribute(
                "categories",
                categoryMapper.toOptionResponseList(categoryQueryService.findActive())
        );
    }

    private QuestionCreateRequest createRequestWithDefaultOptions() {
        QuestionCreateRequest request = new QuestionCreateRequest();
        request.getOptions().add(new AnswerOptionRequest());
        request.getOptions().add(new AnswerOptionRequest());
        return request;
    }
}
