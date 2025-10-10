package kh.edu.cstad.stackquizapi.dto.response;

import lombok.Builder;

@Builder
public record HistoricalLeaderboardResponse(
        String sessionId,
        String sessionCode,
        String sessionName,
        String hostName,
        java.time.LocalDateTime startTime,
        java.time.LocalDateTime endTime,
        Integer totalParticipants,
        Integer totalQuestions,
        String status,
        LeaderboardResponse leaderboard,
        Long lastUpdated
) {
}