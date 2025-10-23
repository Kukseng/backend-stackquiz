package kh.edu.cstad.stackquizapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kh.edu.cstad.stackquizapi.dto.response.AdminDashboardResponse;
import kh.edu.cstad.stackquizapi.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @Operation(
        summary = "Get admin dashboard statistics",
        description = "Get comprehensive statistics for admin dashboard including sessions, participants, quizzes, and engagement metrics",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getAdminDashboard() {
        log.info("GET /api/v1/admin/dashboard - Fetching admin dashboard statistics");
        AdminDashboardResponse stats = adminDashboardService.getAdminDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @Operation(
        summary = "Get admin dashboard statistics by period",
        description = "Get statistics for a specific time period (e.g., last 7 days, last 30 days)",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/dashboard/period")
    public ResponseEntity<AdminDashboardResponse> getAdminDashboardByPeriod(
            @RequestParam(defaultValue = "30") int days) {
        log.info("GET /api/v1/admin/dashboard/period?days={}", days);
        AdminDashboardResponse stats = adminDashboardService.getAdminDashboardStatsByPeriod(days);
        return ResponseEntity.ok(stats);
    }
}

