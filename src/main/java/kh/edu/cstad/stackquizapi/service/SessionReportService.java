package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.SessionReportRequest;
import kh.edu.cstad.stackquizapi.dto.response.SessionReportResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface for generating and retrieving detailed reports for quiz sessions.
 * <p>
 * Provides methods for comprehensive session analytics, participant performance,
 * question-level insights, live session monitoring, and report exports.
 * </p>
 *
 * @author Phou Kukseng
 * @since 1.0
 */
public interface SessionReportService {

    /**
     * Generate a comprehensive report for a specific session.
     *
     * @param sessionCode the session code
     * @param request the report request parameters
     * @return a SessionReportResponse containing session data and insights
     */
    SessionReportResponse generateSessionReport(String sessionCode, SessionReportRequest request);

    /**
     * Get summary statistics for a session.
     *
     * @param sessionCode the session code
     * @return session-level statistics
     */
    SessionReportResponse.SessionStatistics getSessionStatistics(String sessionCode);

    /**
     * Get detailed analysis for each question in the session.
     *
     * @param sessionCode the session code
     * @return a list of question analysis objects
     */
    List<SessionReportResponse.QuestionAnalysis> getQuestionAnalysis(String sessionCode);

    /**
     * Retrieve participant reports with pagination support.
     *
     * @param sessionCode the session code
     * @param request pagination and filter parameters
     * @return a page of participant reports
     */
    Page<SessionReportResponse.ParticipantReport> getParticipantReports(
            String sessionCode,
            SessionReportRequest request
    );

    /**
     * Retrieve a detailed report for an individual participant.
     *
     * @param sessionCode the session code
     * @param participantId the participant's ID
     * @return participant report with detailed performance data
     */
    SessionReportResponse.ParticipantReport getParticipantReport(
            String sessionCode,
            String participantId
    );

    /**
     * Get performance insights and recommendations for a session.
     *
     * @param sessionCode the session code
     * @return performance insights object
     */
    SessionReportResponse.PerformanceInsights getPerformanceInsights(String sessionCode);

    /**
     * Export the session report in a specific format (e.g., PDF, Excel).
     *
     * @param sessionCode the session code
     * @param request export parameters
     * @return byte array of the exported report
     */
    byte[] exportSessionReport(String sessionCode, SessionReportRequest request);

    /**
     * Get real-time progress and statistics for an ongoing session.
     *
     * @param sessionCode the session code
     * @return live session report
     */
    SessionReportResponse getLiveSessionReport(String sessionCode);

    /**
     * Compare multiple sessions and return their reports.
     *
     * @param sessionCodes list of session codes to compare
     * @return list of session report responses for comparison
     */
    List<SessionReportResponse> compareSessionReports(List<String> sessionCodes);

    /**
     * Retrieve the answer history for a specific question in a session.
     *
     * @param sessionCode the session code
     * @param questionId the question ID
     * @return list of participant answers for the question
     */
    List<SessionReportResponse.ParticipantAnswer> getQuestionAnswerHistory(
            String sessionCode,
            String questionId
    );
}
