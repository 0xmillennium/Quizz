package com.quizz.user.dto;

public record UserProfileResponse(
        Long id,
        String fullName,
        String email,
        String role) {
}
