package com.onlinequiz.user.dto;

public record UserSummaryResponse(
        Long id,
        String fullName,
        String email
) {
}
