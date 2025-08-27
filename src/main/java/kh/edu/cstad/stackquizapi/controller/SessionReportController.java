package kh.edu.cstad.stackquizapi.controller;

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

    @PostMapping("/{sessionId}/generate-report")
    @ResponseStatus(HttpStatus.OK)
    public SessionReport generateReport(@PathVariable String sessionId) {
        return sessionReportService.generateReport(sessionId);
    }

    @GetMapping("/{sessionId}/report")
    @ResponseStatus(HttpStatus.OK)
    public SessionReportResponse getReport(@PathVariable String sessionId) {
        return sessionReportService.getReport(sessionId);
    }

    @GetMapping("/reports/{hostId}")
    @ResponseStatus(HttpStatus.OK)
    public List<SessionReportResponse> getHostReports(
            @PathVariable String hostId
    ) {
        return sessionReportService.getHostReports(hostId);
    }
}