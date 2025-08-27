package kh.edu.cstad.stackquizapi.dto.response;

import jakarta.servlet.http.PushBuilder;
import lombok.Builder;
@Builder
// For historical leaderboard queries
public record HistoricalLeaderboardResponse(

        String sessionId,

        String sessionName,

        String hostName,

        java.time.LocalDateTime sessionEndTime,

        LeaderboardResponse finalLeaderboard,

        SessionStats stats

) {
}