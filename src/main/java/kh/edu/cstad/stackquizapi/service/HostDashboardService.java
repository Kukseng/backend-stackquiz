package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.websocket.HostProgressMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.LiveStatsMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.SessionTimerMessage;
import kh.edu.cstad.stackquizapi.dto.request.SessionTimingRequest;
import kh.edu.cstad.stackquizapi.dto.response.HostDashboardResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for managing host dashboard functionality and real-time session controls
 * Provides comprehensive session management and monitoring capabilities
 */
public interface HostDashboardService {
    
    /**
     * Get comprehensive host dashboard data for a session
     */    HostDashboardResponse getHostDashboard(String sessionCode);   
    /**
     * Update session timing settings (start time, end time, question time limits)
     */
    void updateSessionTiming(String sessionId, SessionTimingRequest request);
    
    /**
     * Start session timer and broadcast to participants
     */
    void startSessionTimer(String sessionId);
    
    /**
     * Pause session timer
     */
    void pauseSessionTimer(String sessionId);
    
    /**
     * Resume session timer
     */
    void resumeSessionTimer(String sessionId);
    
    /**
     * End session timer
     */
    void endSessionTimer(String sessionId);
    
    /**
     * Get current session timer status
     */
    SessionTimerMessage getSessionTimer(String sessionId);
    
    /**
     * Broadcast session timer updates to all participants
     */
    void broadcastTimerUpdate(String sessionId);
    
    /**
     * Set dynamic question time limit for current question
     */
    void setQuestionTimeLimit(String sessionId, int timeLimit);
    
    /**
     * Get real-time participant progress for host view
     */
    List<HostProgressMessage.ParticipantProgress> getParticipantProgress(String sessionId);
    
    /**
     * Get current question statistics for host
     */
    Map<String, Object> getCurrentQuestionStats(String sessionId, String questionId);
    
    /**
     * Force advance to next question (override timing)
     */
    void forceAdvanceQuestion(String sessionId);
    
    /**
     * Get session performance analytics
     */
    Map<String, Object> getSessionAnalytics(String sessionId);
    
    /**
     * Schedule automatic session start
     */
    void scheduleSessionStart(String sessionId, LocalDateTime startTime);
    
    /**
     * Schedule automatic session end
     */
    void scheduleSessionEnd(String sessionId, LocalDateTime endTime);
    
    /**
     * Cancel scheduled session events
     */
    void cancelScheduledEvents(String sessionId);
    
    /**
     * Get session timing information
     */
    Map<String, Object> getSessionTiming(String sessionId);
    
    /**
     * Broadcast host progress updates to host interface
     */
    void broadcastHostProgress(String sessionId);
    
    /**
     * Send real-time notifications to host
     */
    void sendHostNotification(String sessionId, String message, String type);
    
    /**
     * Get participant answer distribution for current question
     */
    Map<String, Integer> getAnswerDistribution(String sessionId, String questionId);
    
    /**
     * Export session data for host analysis
     */
    Map<String, Object> exportSessionData(String sessionId);
}
