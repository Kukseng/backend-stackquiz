package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.response.LeaderboardResponse;
import kh.edu.cstad.stackquizapi.dto.websocket.ParticipantRankingMessage;

import java.util.List;
import java.util.Map;

/**
 * Enhanced leaderboard service with comprehensive real-time features
 * Extends basic leaderboard functionality with Kahoot-style real-time updates
 */
public interface EnhancedLeaderboardService {
    
    /**
     * Get enhanced real-time leaderboard with ranking changes and context
     */
    LeaderboardResponse getEnhancedRealTimeLeaderboard(String sessionId, int limit, int offset);
    
    /**
     * Get leaderboard with current question context
     */
    LeaderboardResponse getLeaderboardWithQuestionContext(String sessionId, int currentQuestion);
    
    /**
     * Get participant ranking with detailed position information
     */
    ParticipantRankingMessage getDetailedParticipantRanking(String sessionId, String participantId);
    
    /**
     * Get top performers with rank change indicators
     */
    List<ParticipantRankingMessage> getTopPerformersWithChanges(String sessionId, int limit);
    
    /**
     * Get leaderboard segment around specific participant
     */
    LeaderboardResponse getLeaderboardAroundParticipant(String sessionId, String participantId, int range);
    
    /**
     * Broadcast enhanced leaderboard updates to all participants
     */
    void broadcastEnhancedLeaderboard(String sessionId, String updateType);
    
    /**
     * Send personalized leaderboard to specific participant
     */
    void sendPersonalizedLeaderboard(String sessionId, String participantId);
    
    /**
     * Get leaderboard with performance indicators
     */
    LeaderboardResponse getLeaderboardWithPerformance(String sessionId);
    
    /**
     * Get real-time leaderboard changes since last update
     */
    Map<String, Object> getLeaderboardChanges(String sessionId, long lastUpdateTime);
    
    /**
     * Get leaderboard statistics for host view
     */
    Map<String, Object> getLeaderboardStatistics(String sessionId);
    
    /**
     * Create leaderboard snapshot for specific moment
     */
    void createLeaderboardSnapshot(String sessionId, String snapshotType);
    
    /**
     * Get historical leaderboard snapshots
     */
    List<Map<String, Object>> getLeaderboardSnapshots(String sessionId);
    
    /**
     * Calculate and broadcast rank changes after score update
     */
    void processRankChanges(String sessionId, String participantId, int oldRank, int newRank);
    
    /**
     * Get leaderboard with time-based filtering
     */
    LeaderboardResponse getTimeFilteredLeaderboard(String sessionId, long startTime, long endTime);
    
    /**
     * Get participant performance comparison
     */
    Map<String, Object> getParticipantComparison(String sessionId, String participantId);
    
    /**
     * Get leaderboard with question-by-question breakdown
     */
    Map<String, Object> getDetailedLeaderboardBreakdown(String sessionId);
    
    /**
     * Broadcast leaderboard to host with additional context
     */
    void broadcastHostLeaderboard(String sessionId);
    
    /**
     * Get animated leaderboard data for smooth transitions
     */
    Map<String, Object> getAnimatedLeaderboardData(String sessionId);
    
    /**
     * Calculate leaderboard trends and patterns
     */
    Map<String, Object> getLeaderboardTrends(String sessionId);
    
    /**
     * Get leaderboard with participant avatars and profiles
     */
    LeaderboardResponse getEnrichedLeaderboard(String sessionId);
    
    /**
     * Export leaderboard data in various formats
     */
    Map<String, Object> exportLeaderboardData(String sessionId, String format);
    
    /**
     * Get real-time leaderboard updates for streaming
     */
    void streamLeaderboardUpdates(String sessionId);
    
    /**
     * Calculate and update participant streaks
     */
    void updateParticipantStreaks(String sessionId, String participantId, boolean isCorrect);
    
    /**
     * Get leaderboard with streak information
     */
    LeaderboardResponse getLeaderboardWithStreaks(String sessionId);
}
