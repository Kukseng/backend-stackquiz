package kh.edu.cstad.stackquizapi.controller;

import kh.edu.cstad.stackquizapi.dto.response.SessionSummaryResponse;
import kh.edu.cstad.stackquizapi.service.ReportsHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports-history")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReportsHistoryController {

    private final ReportsHistoryService reportsHistoryService;

    @GetMapping("/my-sessions")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<List<SessionSummaryResponse>> getMySessionSummaries(
            @AuthenticationPrincipal Jwt accessToken) {

        String hostId = accessToken.getSubject();
        log.info("Fetching session summaries for host: {}", hostId);

        List<SessionSummaryResponse> summaries = reportsHistoryService.getHostSessionSummaries(hostId);

        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/my-sessions/filter")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<List<SessionSummaryResponse>> getFilteredSessionSummaries(
            @AuthenticationPrincipal Jwt accessToken,
            @RequestParam(required = false) String status) {

        String hostId = accessToken.getSubject();
        log.info("Fetching filtered session summaries for host: {} with status: {}", hostId, status);

        List<SessionSummaryResponse> summaries = reportsHistoryService.getFilteredSessionSummaries(hostId, status);

        return ResponseEntity.ok(summaries);
    }
}

