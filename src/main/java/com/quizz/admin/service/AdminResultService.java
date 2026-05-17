package com.quizz.admin.service;

import com.quizz.admin.dto.AdminResultDetailResponse;
import com.quizz.admin.dto.AdminResultFilterRequest;
import com.quizz.admin.dto.AdminResultListResponse;

public interface AdminResultService {

    AdminResultListResponse searchResults(AdminResultFilterRequest filter);

    AdminResultDetailResponse getResultDetail(Long attemptId);
}
