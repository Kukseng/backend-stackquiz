package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.websocket.HostProgressMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.LiveStatsMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.ParticipantRankingMessage;

import java.util.List;
import java.util.Map;

public interface RealTimeStatsService {

    /**
     * Calculate and return current session statistics
     */
    LiveStatsMessage calculateLiveStats(String sessionId);

    /**
     * Calculate host progress information
     */
    HostProgressMessage calculateHostProgress(String sessionId, int currentQuestion);

    /**
     * Calculate participant ranking information
     */
    ParticipantRankingMessage calculateParticipantRanking(String sessionId, String participantId);

    /**
     * Get answer distribution for current question
     */
    Map<String, Integer> getAnswerDistribution(String sessionId, String questionId);

    /**
     * Get participant progress for host view
     */
    List<HostProgressMessage.ParticipantProgress> getParticipantProgress(String sessionId);

    /**
     * Calculate session statistics for host
     */
    HostProgressMessage.SessionStatistics calculateSessionStatistics(String sessionId);

    /**
     * Get current leaderboard rankings
     */
    Map<String, Integer> getCurrentRankings(String sessionId);

    /**
     * Check if participant rank changed
     */
    String calculateRankChange(int previousRank, int currentRank);
}
