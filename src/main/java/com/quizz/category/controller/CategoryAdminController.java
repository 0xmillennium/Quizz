package com.quizz.category.controller;

import com.quizz.category.dto.CategoryCreateRequest;
import com.quizz.category.dto.CategoryUpdateRequest;
import com.quizz.category.entity.Category;
import com.quizz.category.mapper.CategoryMapper;
import com.quizz.category.service.CategoryCommandService;
import com.quizz.category.service.CategoryQueryService;
import com.quizz.category.validation.CategoryFormValidator;
import com.quizz.common.web.FlashMessage;
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
 * ADMIN MVC boundary for category management.
 *
 * <p>The controller handles form binding, validation errors, flash messages,
 * and view selection for {@code /admin/categories}. Category lifecycle and
 * persistence rules are delegated to command and query services.</p>
 */
@Controller
@RequestMapping("/admin/categories")
public class CategoryAdminController {

    private final CategoryCommandService categoryCommandService;
    private final CategoryQueryService categoryQueryService;
    private final CategoryMapper categoryMapper;
    private final CategoryFormValidator categoryFormValidator;

    public CategoryAdminController(
            CategoryCommandService categoryCommandService,
            CategoryQueryService categoryQueryService,
            CategoryMapper categoryMapper,
            CategoryFormValidator categoryFormValidator
    ) {
        this.categoryCommandService = categoryCommandService;
        this.categoryQueryService = categoryQueryService;
        this.categoryMapper = categoryMapper;
        this.categoryFormValidator = categoryFormValidator;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryMapper.toResponseList(categoryQueryService.findAll()));
        return "admin/categories/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("categoryCreateRequest", new CategoryCreateRequest());
        return "admin/categories/create";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("categoryCreateRequest") CategoryCreateRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        categoryFormValidator.validateCreate(request, bindingResult);
        if (bindingResult.hasErrors()) {
            return "admin/categories/create";
        }

        categoryCommandService.create(request);
        redirectAttributes.addFlashAttribute(
                "flashMessage",
                FlashMessage.success("Category created successfully.")
        );
        return "redirect:/admin/categories";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Category category = categoryQueryService.getById(id);
        model.addAttribute("categoryId", id);
        model.addAttribute("categoryUpdateRequest", categoryMapper.toUpdateRequest(category));
        return "admin/categories/edit";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("categoryUpdateRequest") CategoryUpdateRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        categoryFormValidator.validateUpdate(id, request, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("categoryId", id);
            return "admin/categories/edit";
        }

        categoryCommandService.update(id, request);
        redirectAttributes.addFlashAttribute(
                "flashMessage",
                FlashMessage.success("Category updated successfully.")
        );
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoryCommandService.activate(id);
        redirectAttributes.addFlashAttribute(
                "flashMessage",
                FlashMessage.success("Category activated successfully.")
        );
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoryCommandService.deactivate(id);
        redirectAttributes.addFlashAttribute(
                "flashMessage",
                FlashMessage.success("Category deactivated successfully.")
        );
        return "redirect:/admin/categories";
    }
}
