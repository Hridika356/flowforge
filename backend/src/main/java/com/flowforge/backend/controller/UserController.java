package com.flowforge.backend.controller;

import com.flowforge.backend.dto.DashboardResponse;
import com.flowforge.backend.dto.UserResponse;
import com.flowforge.backend.security.AuthUser;
import com.flowforge.backend.service.DashboardService;
import com.flowforge.backend.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final DashboardService dashboardService;

    public UserController(UserService userService, DashboardService dashboardService) {
        this.userService = userService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/users/me")
    public UserResponse me(@AuthenticationPrincipal AuthUser user) {
        return userService.me(user.id());
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard(@AuthenticationPrincipal AuthUser user) {
        return dashboardService.forUser(user.id());
    }
}
