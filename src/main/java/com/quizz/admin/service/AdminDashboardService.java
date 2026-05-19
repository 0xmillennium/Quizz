package com.quizz.admin.service;

import com.quizz.admin.dto.AdminDashboardResponse;

/**
 * Read-only service for administrator dashboard metrics.
 *
 * <p>The dashboard aggregates operational counts and recent attempt summaries.
 * It must not mutate domain state, recalculate scores, or expose user email
 * addresses in reporting models.</p>
 */
public interface AdminDashboardService {

    AdminDashboardResponse getDashboard();
}
