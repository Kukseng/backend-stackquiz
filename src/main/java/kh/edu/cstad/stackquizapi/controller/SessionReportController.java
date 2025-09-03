package kh.edu.cstad.stackquizapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kh.edu.cstad.stackquizapi.domain.SessionReport;
import kh.edu.cstad.stackquizapi.dto.response.SessionReportResponse;
import kh.edu.cstad.stackquizapi.service.SessionReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionReportController {

    private final SessionReportService sessionReportService;

    // get current user for profile
    @Operation(summary = "Get all users (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @PostMapping("/{sessionId}/generate-report")
    @ResponseStatus(HttpStatus.OK)
    public SessionReport generateReport(@PathVariable String sessionId) {
        return sessionReportService.generateReport(sessionId);
    }

    // get current user for profile
    @Operation(summary = "Get all users (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @GetMapping("/{sessionId}/report")
    @ResponseStatus(HttpStatus.OK)
    public SessionReportResponse getReport(@PathVariable String sessionId) {
        return sessionReportService.getReport(sessionId);
    }

    // get current user for profile
    @Operation(summary = "Get all users (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @GetMapping("/reports/{hostId}")
    @ResponseStatus(HttpStatus.OK)
    public List<SessionReportResponse> getHostReports(
            @PathVariable String hostId
    ) {
        return sessionReportService.getHostReports(hostId);
    }
}