package com.quizz.admin.dto;

import java.util.List;

public record AdminResultListResponse(
        AdminResultFilterRequest filter,
        AdminPageResponse page,
        List<AdminResultSummaryResponse> results) {
}
