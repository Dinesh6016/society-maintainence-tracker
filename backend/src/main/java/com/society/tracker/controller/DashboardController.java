package com.society.tracker.controller;

import com.society.tracker.dto.DashboardDto;
import com.society.tracker.entity.User;
import com.society.tracker.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin/dashboard")
    public ResponseEntity<DashboardDto> adminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping("/resident/dashboard")
    public ResponseEntity<Map<String, Long>> residentDashboard(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dashboardService.getResidentDashboard(user.getId()));
    }
}
