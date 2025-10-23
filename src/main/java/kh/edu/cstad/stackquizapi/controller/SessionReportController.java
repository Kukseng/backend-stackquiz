package kh.edu.cstad.stackquizapi.controller;

import kh.edu.cstad.stackquizapi.dto.request.SessionReportRequest;
import kh.edu.cstad.stackquizapi.dto.response.SessionReportResponse;
import kh.edu.cstad.stackquizapi.service.SessionReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SessionReportController {

    private final SessionReportService sessionReportService;

    @GetMapping("/session/{sessionCode}")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<SessionReportResponse> getSessionReport(
            @PathVariable String sessionCode,
            @RequestParam(required = false, defaultValue = "DETAILED") SessionReportRequest.ReportType reportType,
            @RequestParam(required = false, defaultValue = "true") Boolean includeDetailedAnswers,
            @RequestParam(required = false, defaultValue = "true") Boolean includePerformanceInsights,
            @RequestParam(required = false, defaultValue = "true") Boolean includeRecommendations) {

        log.info("Generating session report for session: {}", sessionCode);

        SessionReportRequest request = SessionReportRequest.builder()
                .sessionCode(sessionCode)
                .reportType(reportType)
                .includeDetailedAnswers(includeDetailedAnswers)
                .includePerformanceInsights(includePerformanceInsights)
                .includeRecommendations(includeRecommendations)
                .build();

        SessionReportResponse report = sessionReportService.generateSessionReport(sessionCode, request);

        return ResponseEntity.ok(report);
    }

    @GetMapping("/session/{sessionCode}/statistics")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<SessionReportResponse.SessionStatistics> getSessionStatistics(
            @PathVariable String sessionCode) {

        log.info("Getting session statistics for session: {}", sessionCode);

        SessionReportResponse.SessionStatistics statistics =
                sessionReportService.getSessionStatistics(sessionCode);

        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/session/{sessionCode}/questions")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<List<SessionReportResponse.QuestionAnalysis>> getQuestionAnalysis(
            @PathVariable String sessionCode) {

        log.info("Getting question analysis for session: {}", sessionCode);

        List<SessionReportResponse.QuestionAnalysis> analysis =
                sessionReportService.getQuestionAnalysis(sessionCode);

        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/session/{sessionCode}/participants")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<Page<SessionReportResponse.ParticipantReport>> getParticipantReports(
            @PathVariable String sessionCode,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "score") String sortBy,
            @RequestParam(required = false, defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) Double minAccuracy,
            @RequestParam(required = false) Double maxAccuracy,
            @RequestParam(required = false, defaultValue = "ALL") String completionStatus) {

        log.info("Getting participant reports for session: {} (page: {}, size: {})",
                sessionCode, page, size);

        SessionReportRequest request = SessionReportRequest.builder()
                .sessionCode(sessionCode)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .minAccuracy(minAccuracy)
                .maxAccuracy(maxAccuracy)
                .completionStatus(completionStatus)
                .build();

        Page<SessionReportResponse.ParticipantReport> reports =
                sessionReportService.getParticipantReports(sessionCode, request);

        return ResponseEntity.ok(reports);
    }

    @GetMapping("/session/{sessionCode}/participant/{participantId}")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN') or #participantId == authentication.name")
    public ResponseEntity<SessionReportResponse.ParticipantReport> getParticipantReport(
            @PathVariable String sessionCode,
            @PathVariable String participantId) {

        log.info("Getting participant report for session: {}, participant: {}",
                sessionCode, participantId);

        SessionReportResponse.ParticipantReport report =
                sessionReportService.getParticipantReport(sessionCode, participantId);

        return ResponseEntity.ok(report);
    }

    @GetMapping("/session/{sessionCode}/insights")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<SessionReportResponse.PerformanceInsights> getPerformanceInsights(
            @PathVariable String sessionCode) {

        log.info("Getting performance insights for session: {}", sessionCode);

        SessionReportResponse.PerformanceInsights insights =
                sessionReportService.getPerformanceInsights(sessionCode);

        return ResponseEntity.ok(insights);
    }

    @GetMapping("/session/{sessionCode}/live")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<SessionReportResponse> getLiveSessionReport(
            @PathVariable String sessionCode) {

        log.info("Getting live session report for session: {}", sessionCode);

        SessionReportResponse report = sessionReportService.getLiveSessionReport(sessionCode);

        return ResponseEntity.ok(report);
    }

    @GetMapping("/session/{sessionCode}/question/{questionId}/answers")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<List<SessionReportResponse.ParticipantAnswer>> getQuestionAnswerHistory(
            @PathVariable String sessionCode,
            @PathVariable String questionId) {

        log.info("Getting answer history for session: {}, question: {}", sessionCode, questionId);

        List<SessionReportResponse.ParticipantAnswer> answers =
                sessionReportService.getQuestionAnswerHistory(sessionCode, questionId);

        return ResponseEntity.ok(answers);
    }

    @GetMapping("/session/{sessionCode}/export")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportSessionReport(
            @PathVariable String sessionCode,
            @RequestParam(required = false, defaultValue = "PDF") SessionReportRequest.ReportFormat format,
            @RequestParam(required = false, defaultValue = "DETAILED") SessionReportRequest.ReportType reportType) {

        log.info("Exporting session report for session: {} in format: {}", sessionCode, format);

        SessionReportRequest request = SessionReportRequest.builder()
                .sessionCode(sessionCode)
                .format(format)
                .reportType(reportType)
                .includeDetailedAnswers(true)
                .includePerformanceInsights(true)
                .includeRecommendations(true)
                .build();

        byte[] reportData = sessionReportService.exportSessionReport(sessionCode, request);

        HttpHeaders headers = new HttpHeaders();
        String filename = "session_report_" + sessionCode;

        switch (format) {
            case PDF:
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", filename + ".pdf");
                break;
            case CSV:
                headers.setContentType(MediaType.parseMediaType("text/csv"));
                headers.setContentDispositionFormData("attachment", filename + ".csv");
                break;
            case EXCEL:
                headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
                headers.setContentDispositionFormData("attachment", filename + ".xlsx");
                break;
            default:
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", filename + ".bin");
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(reportData);
    }

    @PostMapping("/compare")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<List<SessionReportResponse>> compareSessionReports(
            @RequestBody @Valid List<String> sessionCodes) {

        log.info("Comparing session reports for sessions: {}", sessionCodes);

        if (sessionCodes.size() > 10) {
            return ResponseEntity.badRequest().build();
        }

        List<SessionReportResponse> reports =
                sessionReportService.compareSessionReports(sessionCodes);

        return ResponseEntity.ok(reports);
    }

    @PostMapping("/session/{sessionCode}/custom")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<SessionReportResponse> generateCustomReport(
            @PathVariable String sessionCode,
            @RequestBody @Valid SessionReportRequest request) {

        log.info("Generating custom report for session: {} with request: {}", sessionCode, request);

        // Ensure session code matches
        request.setSessionCode(sessionCode);

        SessionReportResponse report = sessionReportService.generateSessionReport(sessionCode, request);

        return ResponseEntity.ok(report);
    }

    @GetMapping("/session/{sessionCode}/summary")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<SessionReportResponse> getSessionSummary(
            @PathVariable String sessionCode) {

        log.info("Getting session summary for session: {}", sessionCode);

        SessionReportRequest request = SessionReportRequest.builder()
                .sessionCode(sessionCode)
                .reportType(SessionReportRequest.ReportType.SUMMARY)
                .includeDetailedAnswers(false)
                .includePerformanceInsights(true)
                .includeRecommendations(false)
                .build();

        SessionReportResponse report = sessionReportService.generateSessionReport(sessionCode, request);

        return ResponseEntity.ok(report);
    }

    @GetMapping("/session/{sessionCode}/participant/{participantId}/trends")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN') or #participantId == authentication.name")
    public ResponseEntity<SessionReportResponse.PerformanceMetrics> getParticipantTrends(
            @PathVariable String sessionCode,
            @PathVariable String participantId) {

        log.info("Getting performance trends for session: {}, participant: {}",
                sessionCode, participantId);

        SessionReportResponse.ParticipantReport report =
                sessionReportService.getParticipantReport(sessionCode, participantId);

        return ResponseEntity.ok(report.getPerformance());
    }

    @GetMapping("/session/{sessionCode}/difficulty")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<List<SessionReportResponse.QuestionAnalysis>> getQuestionDifficultyAnalysis(
            @PathVariable String sessionCode,
            @RequestParam(required = false) String difficulty) {

        log.info("Getting question difficulty analysis for session: {}", sessionCode);

        List<SessionReportResponse.QuestionAnalysis> analysis =
                sessionReportService.getQuestionAnalysis(sessionCode);

        // Filter by difficulty if specified
        if (difficulty != null && !difficulty.isEmpty()) {
            analysis = analysis.stream()
                    .filter(q -> difficulty.equalsIgnoreCase(q.getDifficulty()))
                    .collect(java.util.stream.Collectors.toList());
        }

        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/session/{sessionCode}/engagement")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<EngagementMetrics> getEngagementMetrics(
            @PathVariable String sessionCode) {

        log.info("Getting engagement metrics for session: {}", sessionCode);

        SessionReportResponse.SessionStatistics stats =
                sessionReportService.getSessionStatistics(sessionCode);

        SessionReportResponse.PerformanceInsights insights =
                sessionReportService.getPerformanceInsights(sessionCode);

        EngagementMetrics metrics = EngagementMetrics.builder()
                .completionRate(stats.getCompletionRate())
                .engagementRate(stats.getEngagementRate())
                .averageAccuracy(stats.getAverageAccuracy())
                .averageResponseTime(stats.getAverageResponseTime())
                .dropoffRate(insights.getDropoffRate())
                .totalParticipants(stats.getTotalParticipants())
                .activeParticipants(stats.getTotalParticipants() - (int)(stats.getTotalParticipants() * insights.getDropoffRate() / 100))
                .build();

        return ResponseEntity.ok(metrics);
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EngagementMetrics {
        private Double completionRate;
        private Double engagementRate;
        private Double averageAccuracy;
        private Double averageResponseTime;
        private Double dropoffRate;
        private Integer totalParticipants;
        private Integer activeParticipants;
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleReportError(RuntimeException ex) {
        log.error("Error generating report: {}", ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .error("REPORT_GENERATION_ERROR")
                .message(ex.getMessage())
                .timestamp(java.time.LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(IllegalArgumentException ex) {
        log.warn("Resource not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error("RESOURCE_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(java.time.LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ErrorResponse {
        private String error;
        private String message;
        private java.time.LocalDateTime timestamp;
    }
}