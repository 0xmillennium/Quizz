package com.quizz.question.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class QuestionUpdateRequest {

    @NotBlank
    @Size(min = 5, max = 1000)
    private String text;

    @NotNull
    private Long categoryId;

    @Valid
    @NotEmpty
    private List<AnswerOptionRequest> options = new ArrayList<>();

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public List<AnswerOptionRequest> getOptions() {
        return options;
    }

    public void setOptions(List<AnswerOptionRequest> options) {
        this.options = options;
    }
}
