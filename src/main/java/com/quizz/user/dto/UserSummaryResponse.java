package com.quizz.user.dto;

public record UserSummaryResponse(
        Long id,
        String fullName,
        String email
) {
}
