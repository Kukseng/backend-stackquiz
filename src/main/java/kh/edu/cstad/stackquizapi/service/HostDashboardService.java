package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.response.QuestionAnalyticsResponse;
import kh.edu.cstad.stackquizapi.dto.websocket.HostProgressMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.LiveStatsMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.SessionTimerMessage;
import kh.edu.cstad.stackquizapi.dto.request.SessionTimingRequest;
import kh.edu.cstad.stackquizapi.dto.response.HostDashboardResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for managing host dashboard functionality and real-time session controls.
 * <p>
 * Provides comprehensive session management and monitoring capabilities,
 * enabling the host to view analytics, control session timing, track progress,
 * and interact with participants in real time.
 * </p>
 *
 * <p>This interface supports features such as:
 * <ul>
 *   <li>Real-time timer and progress broadcasting</li>
 *   <li>Dynamic question and session scheduling</li>
 *   <li>Participant progress and analytics visualization</li>
 *   <li>Exporting session data for host review</li>
 * </ul>
 * </p>
 *
 * @author Phou Kukseng
 * @since 1.0
 */
public interface HostDashboardService {

    /**
     * Retrieves comprehensive host dashboard data for a given session.
     *
     * @param sessionCode the session code identifying the live quiz session
     * @return a detailed response containing host dashboard data
     */
    HostDashboardResponse getHostDashboard(String sessionCode);

    /**
     * Updates session timing settings such as start time, end time, and question time limits.
     *
     * @param sessionId the unique session identifier
     * @param request   the request object containing updated timing configurations
     */
    void updateSessionTiming(String sessionId, SessionTimingRequest request);

    /**
     * Starts the session timer and broadcasts the start event to all participants.
     *
     * @param sessionId the unique session identifier
     */
    void startSessionTimer(String sessionId);

    /**
     * Pauses the ongoing session timer.
     *
     * @param sessionId the unique session identifier
     */
    void pauseSessionTimer(String sessionId);

    /**
     * Resumes a previously paused session timer.
     *
     * @param sessionId the unique session identifier
     */
    void resumeSessionTimer(String sessionId);

    /**
     * Ends the session timer, marking the session as completed.
     *
     * @param sessionId the unique session identifier
     */
    void endSessionTimer(String sessionId);

    /**
     * Retrieves the current session timer status, including remaining time.
     *
     * @param sessionId the unique session identifier
     * @return a message containing current timer details
     */
    SessionTimerMessage getSessionTimer(String sessionId);

    /**
     * Broadcasts the current session timer update to all participants.
     *
     * @param sessionId the unique session identifier
     */
    void broadcastTimerUpdate(String sessionId);

    /**
     * Sets a dynamic time limit for the current question.
     *
     * @param sessionId the unique session identifier
     * @param timeLimit the time limit in seconds for the current question
     */
    void setQuestionTimeLimit(String sessionId, int timeLimit);

    /**
     * Retrieves real-time participant progress for host monitoring.
     *
     * @param sessionId the unique session identifier
     * @return a list of participant progress data
     */
    List<HostProgressMessage.ParticipantProgress> getParticipantProgress(String sessionId);

    /**
     * Retrieves statistics for the current question such as accuracy and participation rate.
     *
     * @param sessionId  the unique session identifier
     * @param questionId the question identifier
     * @return a map of key-value pairs representing question statistics
     */
    Map<String, Object> getCurrentQuestionStats(String sessionId, String questionId);

    /**
     * Forces the session to advance to the next question, overriding any remaining time.
     *
     * @param sessionId the unique session identifier
     */
    void forceAdvanceQuestion(String sessionId);

    /**
     * Retrieves performance analytics for the entire session.
     *
     * @param sessionId the unique session identifier
     * @return a map containing session analytics data
     */
    Map<String, Object> getSessionAnalytics(String sessionId);

    /**
     * Schedules the automatic start of a session at a specific time.
     *
     * @param sessionId  the unique session identifier
     * @param startTime  the scheduled start time
     */
    void scheduleSessionStart(String sessionId, LocalDateTime startTime);

    /**
     * Schedules the automatic end of a session at a specific time.
     *
     * @param sessionId the unique session identifier
     * @param endTime   the scheduled end time
     */
    void scheduleSessionEnd(String sessionId, LocalDateTime endTime);

    /**
     * Cancels any previously scheduled session events (start or end).
     *
     * @param sessionId the unique session identifier
     */
    void cancelScheduledEvents(String sessionId);

    /**
     * Retrieves session timing information including start, end, and elapsed time.
     *
     * @param sessionId the unique session identifier
     * @return a map of timing-related data
     */
    Map<String, Object> getSessionTiming(String sessionId);

    /**
     * Broadcasts real-time host progress updates to the host dashboard interface.
     *
     * @param sessionId the unique session identifier
     */
    void broadcastHostProgress(String sessionId);

    /**
     * Sends real-time notifications to the host dashboard (e.g., system alerts, event reminders).
     *
     * @param sessionId the unique session identifier
     * @param message   the notification message
     * @param type      the notification type (e.g., "INFO", "WARNING", "ERROR")
     */
    void sendHostNotification(String sessionId, String message, String type);

    /**
     * Retrieves the distribution of participant answers for the current question.
     *
     * @param sessionId  the unique session identifier
     * @param questionId the question identifier
     * @return a map where keys are answer options and values are participant counts
     */
    Map<String, Integer> getAnswerDistribution(String sessionId, String questionId);

    /**
     * Exports session data for host analysis (e.g., results, timing, performance metrics).
     *
     * @param sessionId the unique session identifier
     * @return a map containing exported session data or metadata
     */
    Map<String, Object> exportSessionData(String sessionId);

    /**
     * Retrieves Kahoot-style question analytics for SYNC mode.
     * <p>
     * Provides post-question insights such as participation rate, accuracy,
     * and top 3 performers.
     * </p>
     *
     * @param sessionCode the unique session code
     * @return analytics containing question statistics and top performer leaderboard
     */
    QuestionAnalyticsResponse getQuestionAnalytics(String sessionCode);
}

