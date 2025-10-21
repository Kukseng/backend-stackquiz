package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.websocket.ParticipantRankingMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.ScoreUpdateMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.AnswerFeedbackMessage;

import java.util.List;
import java.util.Map;

/**
 * Service for managing real-time participant rankings and score updates
 * Provides Kahoot-style real-time feedback to participants during quiz
 */
public interface RealTimeRankingService {

    /**
     * Update participant score and broadcast ranking changes to all participants
     * Called after each answer submission
     */
    void updateParticipantScoreAndRanking(String sessionId, String participantId,
                                          String participantNickname, int newScore,
                                          boolean isCorrect, int pointsEarned);

    /**
     * Calculate and send individual ranking update to specific participant
     */
    void sendRankingUpdateToParticipant(String sessionId, String participantId);

    /**
     * Broadcast current leaderboard with ranking changes to all participants
     */
    void broadcastRankingUpdates(String sessionId);

    /**
     * Send score update message to specific participant
     */
    void sendScoreUpdate(String sessionId, String participantId, int pointsEarned,
                         int totalScore, boolean isCorrect);

    /**
     * Send answer feedback with ranking information to participant
     */
    void sendAnswerFeedback(String sessionId, String participantId, String questionId,
                            boolean isCorrect, int pointsEarned, int timeTaken,
                            String selectedOptionId, String correctOptionId);

    /**
     * Get current participant rankings for session
     */
    Map<String, Integer> getCurrentRankings(String sessionId);

    /**
     * Get participant's current rank and position information
     */
    ParticipantRankingMessage getParticipantRanking(String sessionId, String participantId);

    /**
     * Calculate rank change for participant (UP, DOWN, SAME, NEW)
     */
    String calculateRankChange(String sessionId, String participantId, int newRank);

    /**
     * Store previous rankings for rank change calculation
     */
    void storePreviousRankings(String sessionId, Map<String, Integer> rankings);

    /**
     * Get top performers for session (top 3 or 5)
     */
    List<ParticipantRankingMessage> getTopPerformers(String sessionId, int limit);

    /**
     * Send real-time progress update to participant showing their position
     */
    void sendProgressUpdate(String sessionId, String participantId, int currentQuestion,
                            int totalQuestions);

    /**
     * Broadcast session statistics to all participants
     */
    void broadcastSessionStats(String sessionId);

    /**
     * Initialize ranking system for new session
     */
    void initializeSessionRankings(String sessionId);

    /**
     * Clean up ranking data when session ends
     */
    void cleanupSessionRankings(String sessionId);
}
