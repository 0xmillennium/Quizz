package com.quizz.auth.controller;

import com.quizz.auth.dto.RegisterRequest;
import com.quizz.auth.service.RegistrationService;
import com.quizz.auth.validation.RegistrationValidator;
import com.quizz.common.web.FlashMessage;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistrationController {

    private final RegistrationService registrationService;
    private final RegistrationValidator registrationValidator;

    public RegistrationController(RegistrationService registrationService, RegistrationValidator registrationValidator) {
        this.registrationService = registrationService;
        this.registrationValidator = registrationValidator;
    }

    @GetMapping("/register")
    public String registerForm(org.springframework.ui.Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        registrationValidator.validate(request, bindingResult);
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        registrationService.register(request);
        redirectAttributes.addFlashAttribute(
                "flashMessage",
                FlashMessage.success("Registration successful. You can now log in.")
        );
        return "redirect:/login";
    }
}
