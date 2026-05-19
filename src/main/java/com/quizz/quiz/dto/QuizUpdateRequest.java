package com.quizz.quiz.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class QuizUpdateRequest {

    @NotBlank @Size(min = 2, max = 150) private String title;

    @Size(max = 1000) private String description;

    @NotNull private Long categoryId;

    @NotNull @Min(1) @Max(180) private Integer durationMinutes;

    @NotNull @Min(1) private Integer questionCount = 1;

    @NotNull @Min(1) private Integer attemptLimit = 3;

    @NotNull @Min(1) private Integer retakeCooldownMinutes = 1440;

    @NotEmpty private List<Long> questionIds = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public Integer getAttemptLimit() {
        return attemptLimit;
    }

    public void setAttemptLimit(Integer attemptLimit) {
        this.attemptLimit = attemptLimit;
    }

    public Integer getRetakeCooldownMinutes() {
        return retakeCooldownMinutes;
    }

    public void setRetakeCooldownMinutes(Integer retakeCooldownMinutes) {
        this.retakeCooldownMinutes = retakeCooldownMinutes;
    }

    public List<Long> getQuestionIds() {
        return questionIds;
    }

    public void setQuestionIds(List<Long> questionIds) {
        this.questionIds = questionIds;
    }
}
