package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.websocket.ParticipantRankingMessage;

import java.util.List;
import java.util.Map;

/**
 * Service for managing real-time participant rankings and score updates.
 * <p>
 * Provides Kahoot-style instant feedback to participants during quizzes,
 * including ranking updates, score notifications, and leaderboard broadcasts.
 * </p>
 *
 * @author Phou Kukseng
 * @since 1.0
 */
public interface RealTimeRankingService {

    /**
     * Update a participant's score and broadcast ranking changes to all participants.
     * Typically called after each answer submission.
     *
     * @param sessionId the session ID
     * @param participantId the participant's ID
     * @param participantNickname the participant's display nickname
     * @param newScore the participant's updated score
     * @param isCorrect whether the answer was correct
     * @param pointsEarned points earned for the answer
     */
    void updateParticipantScoreAndRanking(String sessionId, String participantId,
                                          String participantNickname, int newScore,
                                          boolean isCorrect, int pointsEarned);

    /**
     * Calculate and send an individual ranking update to a specific participant.
     *
     * @param sessionId the session ID
     * @param participantId the participant's ID
     */
    void sendRankingUpdateToParticipant(String sessionId, String participantId);

    /**
     * Broadcast the current leaderboard with updated rankings to all participants.
     *
     * @param sessionId the session ID
     */
    void broadcastRankingUpdates(String sessionId);

    /**
     * Send a score update message to a specific participant.
     *
     * @param sessionId the session ID
     * @param participantId the participant's ID
     * @param pointsEarned points earned for the latest answer
     * @param totalScore participant's total score
     * @param isCorrect whether the answer was correct
     */
    void sendScoreUpdate(String sessionId, String participantId, int pointsEarned,
                         int totalScore, boolean isCorrect);

    /**
     * Send detailed answer feedback with ranking information to a participant.
     *
     * @param sessionId the session ID
     * @param participantId the participant's ID
     * @param questionId the question ID
     * @param isCorrect whether the answer was correct
     * @param pointsEarned points earned
     * @param timeTaken time taken to answer
     * @param selectedOptionId participant's selected option
     * @param correctOptionId the correct option
     */
    void sendAnswerFeedback(String sessionId, String participantId, String questionId,
                            boolean isCorrect, int pointsEarned, int timeTaken,
                            String selectedOptionId, String correctOptionId);

    /**
     * Get current participant rankings for a session.
     *
     * @param sessionId the session ID
     * @return a map of participant IDs to their current rank
     */
    Map<String, Integer> getCurrentRankings(String sessionId);

    /**
     * Get detailed ranking information for a specific participant.
     *
     * @param sessionId the session ID
     * @param participantId the participant's ID
     * @return participant ranking message containing rank and position
     */
    ParticipantRankingMessage getParticipantRanking(String sessionId, String participantId);

    /**
     * Calculate rank change for a participant compared to previous ranking.
     *
     * @param sessionId the session ID
     * @param participantId the participant's ID
     * @param newRank the participant's new rank
     * @return rank change indicator: "UP", "DOWN", "SAME", or "NEW"
     */
    String calculateRankChange(String sessionId, String participantId, int newRank);

    /**
     * Store previous session rankings for rank change calculations.
     *
     * @param sessionId the session ID
     * @param rankings map of participant IDs to their previous ranks
     */
    void storePreviousRankings(String sessionId, Map<String, Integer> rankings);

    /**
     * Get top performers for a session.
     *
     * @param sessionId the session ID
     * @param limit number of top participants to retrieve
     * @return list of participant ranking messages
     */
    List<ParticipantRankingMessage> getTopPerformers(String sessionId, int limit);

    /**
     * Send real-time progress updates to a participant.
     *
     * @param sessionId the session ID
     * @param participantId the participant's ID
     * @param currentQuestion current question number
     * @param totalQuestions total number of questions in the session
     */
    void sendProgressUpdate(String sessionId, String participantId, int currentQuestion,
                            int totalQuestions);

    /**
     * Broadcast session-wide statistics to all participants.
     *
     * @param sessionId the session ID
     */
    void broadcastSessionStats(String sessionId);

    /**
     * Initialize the ranking system for a new session.
     *
     * @param sessionId the session ID
     */
    void initializeSessionRankings(String sessionId);

    /**
     * Clean up ranking data when a session ends.
     *
     * @param sessionId the session ID
     */
    void cleanupSessionRankings(String sessionId);
}
