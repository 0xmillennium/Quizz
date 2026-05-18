package com.quizz.admin.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.quizz.admin.dto.AdminDashboardResponse;
import com.quizz.admin.service.AdminDashboardService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AdminDashboardControllerTest {

    @Mock
    private AdminDashboardService adminDashboardService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        AdminDashboardController controller = new AdminDashboardController(adminDashboardService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .apply(springSecurity(AdminControllerSecurityTestSupport.springSecurityFilterChain()))
                .build();
    }

    @Test
    void getAdminAsAdminReturnsDashboardView() throws Exception {
        AdminDashboardResponse dashboard = dashboard();
        when(adminDashboardService.getDashboard()).thenReturn(dashboard);

        mockMvc.perform(get("/admin").with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attribute("dashboard", dashboard));
    }

    @Test
    void getAdminDashboardAsAdminReturnsDashboardView() throws Exception {
        AdminDashboardResponse dashboard = dashboard();
        when(adminDashboardService.getDashboard()).thenReturn(dashboard);

        mockMvc.perform(get("/admin/dashboard").with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attribute("dashboard", dashboard));
    }

    @Test
    void getAdminAsUserIsForbidden() throws Exception {
        mockMvc.perform(get("/admin").with(user("user@example.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAdminAnonymousRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    private AdminDashboardResponse dashboard() {
        return new AdminDashboardResponse(
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0.0,
                List.of()
        );
    }
}
