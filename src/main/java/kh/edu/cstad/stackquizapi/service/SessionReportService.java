package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.SessionReportRequest;
import kh.edu.cstad.stackquizapi.dto.response.SessionReportResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SessionReportService {

    /**
     * Generate comprehensive session report
     */
    SessionReportResponse generateSessionReport(String sessionCode, SessionReportRequest request);

    /**
     * Get session summary statistics
     */
    SessionReportResponse.SessionStatistics getSessionStatistics(String sessionCode);

    /**
     * Get detailed question analysis
     */
    List<SessionReportResponse.QuestionAnalysis> getQuestionAnalysis(String sessionCode);

    /**
     * Get participant reports with pagination
     */
    Page<SessionReportResponse.ParticipantReport> getParticipantReports(
            String sessionCode,
            SessionReportRequest request
    );

    /**
     * Get individual participant detailed report
     */
    SessionReportResponse.ParticipantReport getParticipantReport(
            String sessionCode,
            String participantId
    );

    /**
     * Get performance insights and recommendations
     */
    SessionReportResponse.PerformanceInsights getPerformanceInsights(String sessionCode);

    /**
     * Export session report in different formats
     */
    byte[] exportSessionReport(String sessionCode, SessionReportRequest request);

    /**
     * Get real-time session progress (for ongoing sessions)
     */
    SessionReportResponse getLiveSessionReport(String sessionCode);

    /**
     * Compare multiple sessions
     */
    List<SessionReportResponse> compareSessionReports(List<String> sessionCodes);

    /**
     * Get participant answer history for a specific question
     */
    List<SessionReportResponse.ParticipantAnswer> getQuestionAnswerHistory(
            String sessionCode,
            String questionId
    );
}