package com.quizz.admin.dto;

import java.time.LocalDate;

public class AdminResultFilterRequest {

    private Long userId;
    private Long quizId;
    private Long categoryId;
    private String status;
    private LocalDate startedFrom;
    private LocalDate startedTo;
    private Integer page;
    private Integer size;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getStartedFrom() {
        return startedFrom;
    }

    public void setStartedFrom(LocalDate startedFrom) {
        this.startedFrom = startedFrom;
    }

    public LocalDate getStartedTo() {
        return startedTo;
    }

    public void setStartedTo(LocalDate startedTo) {
        this.startedTo = startedTo;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
