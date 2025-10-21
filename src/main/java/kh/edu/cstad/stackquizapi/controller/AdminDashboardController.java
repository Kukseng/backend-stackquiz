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

/**
 * Controller for Admin Dashboard endpoints
 * Provides comprehensive statistics for system administrators
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    /**
     * Get comprehensive admin dashboard statistics
     * 
     * Returns:
     * - Total sessions (all time)
     * - Active sessions (currently running)
     * - Completed sessions
     * - Total participants (across all sessions)
     * - Active participants (currently in sessions)
     * - Total unique participants
     * - Total quizzes
     * - Total questions
     * - Total answers submitted
     * - Average participants per session
     * - Average session duration
     * - Overall accuracy rate
     * - Sessions today/this week/this month
     * - Participants today/this week/this month
     * - Top 5 most popular quizzes
     * - Recent activity
     * 
     * @return AdminDashboardResponse with all statistics
     */
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

    /**
     * Get admin dashboard statistics for a specific time period
     * 
     * @param days Number of days to look back (default: 30)
     * @return AdminDashboardResponse with filtered statistics
     */
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

