package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.websocket.HostProgressMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.LiveStatsMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.ParticipantRankingMessage;

import java.util.List;
import java.util.Map;

/**
 * Service interface for calculating and providing real-time statistics
 * during quiz sessions.
 * <p>
 * Includes participant progress, host progress, answer distributions,
 * leaderboard rankings, and rank change calculations for live updates.
 * </p>
 *
 * @author Phou Kukseng
 * @since 1.0
 */
public interface RealTimeStatsService {

    /**
     * Calculate and return the current live statistics for a session.
     *
     * @param sessionId the session ID
     * @return a LiveStatsMessage containing session statistics
     */
    LiveStatsMessage calculateLiveStats(String sessionId);

    /**
     * Calculate host progress information for the current question.
     *
     * @param sessionId the session ID
     * @param currentQuestion the current question number
     * @return a HostProgressMessage with host progress details
     */
    HostProgressMessage calculateHostProgress(String sessionId, int currentQuestion);

    /**
     * Calculate participant ranking information for a given session.
     *
     * @param sessionId the session ID
     * @param participantId the participant's ID
     * @return a ParticipantRankingMessage with the participant's rank and details
     */
    ParticipantRankingMessage calculateParticipantRanking(String sessionId, String participantId);

    /**
     * Get the distribution of answers for the current question.
     *
     * @param sessionId the session ID
     * @param questionId the question ID
     * @return a map of option IDs to the number of participants who selected each
     */
    Map<String, Integer> getAnswerDistribution(String sessionId, String questionId);

    /**
     * Retrieve participant progress for host view.
     *
     * @param sessionId the session ID
     * @return a list of ParticipantProgress objects for all participants
     */
    List<HostProgressMessage.ParticipantProgress> getParticipantProgress(String sessionId);

    /**
     * Calculate session-wide statistics for host monitoring.
     *
     * @param sessionId the session ID
     * @return a SessionStatistics object summarizing session performance
     */
    HostProgressMessage.SessionStatistics calculateSessionStatistics(String sessionId);

    /**
     * Get the current leaderboard rankings for a session.
     *
     * @param sessionId the session ID
     * @return a map of participant IDs to their current rank
     */
    Map<String, Integer> getCurrentRankings(String sessionId);

    /**
     * Determine if a participant's rank has changed.
     *
     * @param previousRank the participant's previous rank
     * @param currentRank the participant's current rank
     * @return a string indicating rank change: "UP", "DOWN", "SAME", or "NEW"
     */
    String calculateRankChange(int previousRank, int currentRank);
}
