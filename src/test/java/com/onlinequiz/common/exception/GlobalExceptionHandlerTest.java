package com.onlinequiz.common.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ThrowingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void notFoundExceptionReturns404View() throws Exception {
        mockMvc.perform(get("/throw/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("message", "missing"));
    }

    @Test
    void businessRuleExceptionReturns400View() throws Exception {
        mockMvc.perform(get("/throw/business-rule"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error/400"))
                .andExpect(model().attribute("message", "invalid"));
    }

    @Test
    void duplicateResourceExceptionReturns409View() throws Exception {
        mockMvc.perform(get("/throw/duplicate"))
                .andExpect(status().isConflict())
                .andExpect(view().name("error/409"))
                .andExpect(model().attribute("message", "duplicate"));
    }

    @Test
    void forbiddenOperationExceptionReturns403View() throws Exception {
        mockMvc.perform(get("/throw/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/403"))
                .andExpect(model().attribute("message", "forbidden"));
    }

    @Controller
    private static class ThrowingController {

        @GetMapping("/throw/not-found")
        String throwNotFound() {
            throw new NotFoundException("missing");
        }

        @GetMapping("/throw/business-rule")
        String throwBusinessRule() {
            throw new BusinessRuleException("invalid");
        }

        @GetMapping("/throw/duplicate")
        String throwDuplicate() {
            throw new DuplicateResourceException("duplicate");
        }

        @GetMapping("/throw/forbidden")
        String throwForbidden() {
            throw new ForbiddenOperationException("forbidden");
        }
    }
}
