package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.response.LeaderboardResponse;
import kh.edu.cstad.stackquizapi.dto.websocket.ParticipantRankingMessage;

import java.util.List;
import java.util.Map;

/**
 * Enhanced leaderboard service with comprehensive real-time features.
 * <p>
 * Extends basic leaderboard functionality with advanced, Kahoot-style
 * real-time updates, dynamic ranking, and detailed performance analytics.
 * </p>
 *
 * <p>This service provides:
 * <ul>
 *   <li>Real-time leaderboard synchronization for participants and hosts</li>
 *   <li>Detailed participant ranking and performance metrics</li>
 *   <li>Historical snapshots and trend analysis</li>
 *   <li>Export and visualization support</li>
 * </ul>
 * </p>
 *
 * @author Phou Kukseng
 * @since 1.0
 */
public interface EnhancedLeaderboardService {

    /**
     * Retrieves an enhanced real-time leaderboard with ranking changes and context.
     *
     * @param sessionId the game session identifier
     * @param limit     the maximum number of participants to return
     * @param offset    the pagination offset
     * @return a response containing the enhanced real-time leaderboard
     */
    LeaderboardResponse getEnhancedRealTimeLeaderboard(String sessionId, int limit, int offset);

    /**
     * Retrieves the leaderboard with current question context.
     *
     * @param sessionId        the game session identifier
     * @param currentQuestion  the current question index
     * @return a response containing leaderboard data with question context
     */
    LeaderboardResponse getLeaderboardWithQuestionContext(String sessionId, int currentQuestion);

    /**
     * Retrieves a participant's detailed ranking position and statistics.
     *
     * @param sessionId      the game session identifier
     * @param participantId  the participant identifier
     * @return detailed participant ranking information
     */
    ParticipantRankingMessage getDetailedParticipantRanking(String sessionId, String participantId);

    /**
     * Retrieves top performers with indicators showing rank changes since the last update.
     *
     * @param sessionId the game session identifier
     * @param limit     the number of top performers to include
     * @return a list of participants with rank change data
     */
    List<ParticipantRankingMessage> getTopPerformersWithChanges(String sessionId, int limit);

    /**
     * Retrieves a segment of the leaderboard centered around a specific participant.
     *
     * @param sessionId      the game session identifier
     * @param participantId  the participant identifier
     * @param range          the number of participants above and below to include
     * @return a response containing the localized leaderboard segment
     */
    LeaderboardResponse getLeaderboardAroundParticipant(String sessionId, String participantId, int range);

    /**
     * Broadcasts enhanced leaderboard updates to all participants in a session.
     *
     * @param sessionId  the game session identifier
     * @param updateType the type of leaderboard update (e.g., "rank_change", "score_update")
     */
    void broadcastEnhancedLeaderboard(String sessionId, String updateType);

    /**
     * Sends a personalized leaderboard to a specific participant.
     *
     * @param sessionId      the game session identifier
     * @param participantId  the participant identifier
     */
    void sendPersonalizedLeaderboard(String sessionId, String participantId);

    /**
     * Retrieves leaderboard data with performance indicators like accuracy or average speed.
     *
     * @param sessionId the game session identifier
     * @return leaderboard with performance metrics
     */
    LeaderboardResponse getLeaderboardWithPerformance(String sessionId);

    /**
     * Retrieves real-time leaderboard changes since a specific timestamp.
     *
     * @param sessionId       the game session identifier
     * @param lastUpdateTime  the last update timestamp in milliseconds
     * @return a map containing updated leaderboard elements
     */
    Map<String, Object> getLeaderboardChanges(String sessionId, long lastUpdateTime);

    /**
     * Retrieves leaderboard statistics for the host's analytical view.
     *
     * @param sessionId the game session identifier
     * @return a map of aggregated leaderboard statistics
     */
    Map<String, Object> getLeaderboardStatistics(String sessionId);

    /**
     * Creates a snapshot of the leaderboard at a specific moment in time.
     *
     * @param sessionId    the game session identifier
     * @param snapshotType the type of snapshot (e.g., "end_of_round", "final")
     */
    void createLeaderboardSnapshot(String sessionId, String snapshotType);

    /**
     * Retrieves a list of historical leaderboard snapshots.
     *
     * @param sessionId the game session identifier
     * @return a list of snapshot metadata or stored leaderboard states
     */
    List<Map<String, Object>> getLeaderboardSnapshots(String sessionId);

    /**
     * Calculates and broadcasts rank changes after a participant's score is updated.
     *
     * @param sessionId      the game session identifier
     * @param participantId  the participant identifier
     * @param oldRank        the participant's previous rank
     * @param newRank        the participant's new rank
     */
    void processRankChanges(String sessionId, String participantId, int oldRank, int newRank);

    /**
     * Retrieves a leaderboard filtered by a time range.
     *
     * @param sessionId the game session identifier
     * @param startTime the start timestamp
     * @param endTime   the end timestamp
     * @return the filtered leaderboard response
     */
    LeaderboardResponse getTimeFilteredLeaderboard(String sessionId, long startTime, long endTime);

    /**
     * Compares a participant’s performance with others.
     *
     * @param sessionId      the game session identifier
     * @param participantId  the participant identifier
     * @return a map containing comparative performance data
     */
    Map<String, Object> getParticipantComparison(String sessionId, String participantId);

    /**
     * Retrieves a detailed leaderboard with per-question breakdowns.
     *
     * @param sessionId the game session identifier
     * @return a map containing question-by-question performance data
     */
    Map<String, Object> getDetailedLeaderboardBreakdown(String sessionId);

    /**
     * Broadcasts an enriched leaderboard view to the host with extra session context.
     *
     * @param sessionId the game session identifier
     */
    void broadcastHostLeaderboard(String sessionId);

    /**
     * Retrieves data for animating leaderboard transitions smoothly.
     *
     * @param sessionId the game session identifier
     * @return a map containing animation frame or transition data
     */
    Map<String, Object> getAnimatedLeaderboardData(String sessionId);

    /**
     * Calculates leaderboard trends and patterns over time.
     *
     * @param sessionId the game session identifier
     * @return a map containing trend and pattern analytics
     */
    Map<String, Object> getLeaderboardTrends(String sessionId);

    /**
     * Retrieves leaderboard data enriched with participant avatars and profiles.
     *
     * @param sessionId the game session identifier
     * @return leaderboard with profile and avatar information
     */
    LeaderboardResponse getEnrichedLeaderboard(String sessionId);

    /**
     * Exports leaderboard data into a specified format (e.g., JSON, CSV, PDF).
     *
     * @param sessionId the game session identifier
     * @param format    the export format
     * @return a map containing export file metadata or data
     */
    Map<String, Object> exportLeaderboardData(String sessionId, String format);

    /**
     * Streams real-time leaderboard updates to connected clients.
     *
     * @param sessionId the game session identifier
     */
    void streamLeaderboardUpdates(String sessionId);

    /**
     * Calculates and updates participant streaks (e.g., correct answer streaks).
     *
     * @param sessionId      the game session identifier
     * @param participantId  the participant identifier
     * @param isCorrect      whether the participant's last answer was correct
     */
    void updateParticipantStreaks(String sessionId, String participantId, boolean isCorrect);

    /**
     * Retrieves leaderboard data including participants’ current streaks.
     *
     * @param sessionId the game session identifier
     * @return leaderboard including streak information
     */
    LeaderboardResponse getLeaderboardWithStreaks(String sessionId);
}

