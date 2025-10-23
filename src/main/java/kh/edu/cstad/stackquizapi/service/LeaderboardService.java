package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.LeaderboardRequest;
import kh.edu.cstad.stackquizapi.dto.request.HistoricalLeaderboardRequest;
import kh.edu.cstad.stackquizapi.dto.response.*;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/**
 * Service interface for managing real-time and historical leaderboard operations.
 * <p>
 * Provides functionality for handling live leaderboard updates (Redis-based),
 * as well as persistent leaderboard history and analytics (Database-based).
 * </p>
 *
 * <p>Main features include:
 * <ul>
 *   <li>Real-time leaderboard synchronization and participant ranking</li>
 *   <li>Historical leaderboard retrieval for completed sessions</li>
 *   <li>Session lifecycle management for leaderboard tracking</li>
 *   <li>Statistical analysis and performance summaries</li>
 * </ul>
 * </p>
 *
 * @author Phou Kukseng
 * @since 1.0
 */
public interface LeaderboardService {

    /**
     * Retrieves the real-time leaderboard for an active session.
     *
     * @param request the leaderboard request containing session and filter details
     * @return a response containing the current leaderboard state
     */
    LeaderboardResponse getRealTimeLeaderboard(LeaderboardRequest request);

    /**
     * Retrieves the rank and performance information of a specific participant.
     *
     * @param sessionId      the unique session identifier
     * @param participantId  the participant identifier
     * @return a response containing the participant’s rank and score details
     */
    ParticipantRankResponse getParticipantRank(String sessionId, String participantId);

    /**
     * Retrieves the top three participants (podium) for a given session.
     *
     * @param sessionId the unique session identifier
     * @return a response containing podium data (top 3 participants)
     */
    LeaderboardResponse getPodium(String sessionId);

    /**
     * Removes a participant from the real-time leaderboard.
     *
     * @param sessionId      the unique session identifier
     * @param participantId  the participant identifier
     */
    void removeParticipant(String sessionId, String participantId);

    /**
     * Retrieves a list of historical leaderboards for a given user or time period.
     *
     * @param request      the request containing filtering and sorting parameters
     * @param accessToken  the authenticated user's JWT token
     * @return a list of historical leaderboard responses
     */
    List<HistoricalLeaderboardResponse> getHistoricalLeaderboards(HistoricalLeaderboardRequest request, Jwt accessToken);

    /**
     * Retrieves a detailed session report for a specific completed session.
     *
     * @param sessionId the unique session identifier
     * @return a historical leaderboard response containing session summary and statistics
     */
    HistoricalLeaderboardResponse getSessionReport(String sessionId);

    /**
     * Initializes a new leaderboard for a session when it begins.
     *
     * @param sessionId the unique session identifier
     */
    void initializeSessionLeaderboard(String sessionId);

    /**
     * Finalizes the leaderboard for a session after it ends.
     *
     * @param sessionId the unique session identifier
     */
    void finalizeSessionLeaderboard(String sessionId);

    /**
     * Clears all leaderboard data for a session (used for cleanup or reset).
     *
     * @param sessionId the unique session identifier
     */
    void clearSessionLeaderboard(String sessionId);

    /**
     * Updates a participant’s score and recalculates rankings.
     *
     * @param sessionId      the unique session identifier
     * @param participantId  the participant identifier
     * @param nickname       the participant’s display name
     * @param newScore       the updated score value
     */
    void updateParticipantScore(String sessionId, String participantId, String nickname, int newScore);

    /**
     * Retrieves statistical data and insights for a specific session.
     *
     * @param sessionId the unique session identifier
     * @return session statistics including averages, top scores, and trends
     */
    SessionStats getSessionStatistics(String sessionId);
}