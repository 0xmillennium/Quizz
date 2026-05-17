package com.quizz.admin.dto;

public record AdminPageResponse(
        int page,
        int size,
        long totalItems,
        int totalPages,
        boolean hasPrevious,
        boolean hasNext
) {
}
