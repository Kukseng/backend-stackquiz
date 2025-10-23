package kh.edu.cstad.stackquizapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kh.edu.cstad.stackquizapi.dto.response.UserActivityResponse;
import kh.edu.cstad.stackquizapi.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/analytics")
public class UserActivityController {

    private final UserActivityService userActivityService;

    @Operation(
            summary = "Get current user activity statistics",
            description = "Returns comprehensive activity data for the authenticated user including quiz count, session count, participant count, and time-series data for graphs",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/activity")
    public UserActivityResponse getUserActivity(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Fetching activity statistics for user: {}", userId);
        return userActivityService.getUserActivity(userId);
    }

    @Operation(
            summary = "Get user activity for specific time range",
            description = "Returns activity data filtered by time range (7days, 30days, 90days, 1year, all)",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/activity/{timeRange}")
    public UserActivityResponse getUserActivityByTimeRange(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String timeRange) {
        String userId = jwt.getSubject();
        log.info("Fetching activity statistics for user: {} with time range: {}", userId, timeRange);
        return userActivityService.getUserActivityByTimeRange(userId, timeRange);
    }
}

