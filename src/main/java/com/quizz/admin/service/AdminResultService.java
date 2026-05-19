package com.quizz.admin.service;

import com.quizz.admin.dto.AdminResultDetailResponse;
import com.quizz.admin.dto.AdminResultFilterRequest;
import com.quizz.admin.dto.AdminResultListResponse;

/**
 * Read-only service for administrator attempt-result reporting.
 *
 * <p>
 * Result reports are built from attempt snapshots and persisted score
 * counters. The service may expose correct-answer details to admins, but must
 * not recalculate scores or expose user email addresses.
 * </p>
 */
public interface AdminResultService {

    AdminResultListResponse searchResults(AdminResultFilterRequest filter);

    AdminResultDetailResponse getResultDetail(Long attemptId);
}
