package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.LeaderboardRequest;
import kh.edu.cstad.stackquizapi.dto.request.HistoricalLeaderboardRequest;
import kh.edu.cstad.stackquizapi.dto.response.*;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public interface LeaderboardService {

    // Real-time operations (Redis-based)
    LeaderboardResponse getRealTimeLeaderboard(LeaderboardRequest request);

    ParticipantRankResponse getParticipantRank(String sessionId, String participantId);

    LeaderboardResponse getPodium(String sessionId);

    void removeParticipant(String sessionId, String participantId);

    // Historical operations (Database-based)
    List<HistoricalLeaderboardResponse> getHistoricalLeaderboards(HistoricalLeaderboardRequest request, Jwt accessToken);

    HistoricalLeaderboardResponse getSessionReport(String sessionId);

    // Session management
    void initializeSessionLeaderboard(String sessionId);

    void finalizeSessionLeaderboard(String sessionId);

    void clearSessionLeaderboard(String sessionId);

    void updateParticipantScore(String sessionId, String participantId, String nickname, int newScore);
    // Analytics
    SessionStats getSessionStatistics(String sessionId);
}