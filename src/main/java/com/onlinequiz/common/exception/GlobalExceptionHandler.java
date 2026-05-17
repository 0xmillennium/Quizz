package com.onlinequiz.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NotFoundException exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        return "error/404";
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleForbidden(ForbiddenOperationException exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        return "error/403";
    }

    @ExceptionHandler(UnauthorizedOperationException.class)
    public String handleUnauthorized(UnauthorizedOperationException exception) {
        return "redirect:/login";
    }

    @ExceptionHandler(BusinessRuleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBusinessRule(BusinessRuleException exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        return "error/400";
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicateResource(DuplicateResourceException exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        return "error/409";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneric(Exception exception, Model model) {
        log.error("Unhandled application exception", exception);
        model.addAttribute("message", "An unexpected error occurred.");
        return "error/500";
    }
}
