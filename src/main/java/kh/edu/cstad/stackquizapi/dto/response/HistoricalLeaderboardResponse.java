package kh.edu.cstad.stackquizapi.dto.response;

import lombok.Builder;

@Builder
public record HistoricalLeaderboardResponse(

        String sessionId,

        String sessionName,

        String hostName,

        java.time.LocalDateTime sessionEndTime,

        LeaderboardResponse finalLeaderboard,

        SessionStats stats

) {
}