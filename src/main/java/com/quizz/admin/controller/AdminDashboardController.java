package com.quizz.admin.controller;

import com.quizz.admin.service.AdminDashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ADMIN MVC boundary for dashboard reporting.
 *
 * <p>The controller renders dashboard views from {@link AdminDashboardService}
 * read models only. It does not mutate domain state or recalculate scores.</p>
 */
@Controller
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping({"/admin", "/admin/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("dashboard", adminDashboardService.getDashboard());
        return "admin/dashboard";
    }
}
