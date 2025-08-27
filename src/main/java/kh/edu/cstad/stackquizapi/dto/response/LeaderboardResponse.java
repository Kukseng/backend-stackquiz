package kh.edu.cstad.stackquizapi.dto.response;


import lombok.Builder;
import java.util.List;

@Builder
public record LeaderboardResponse(
        String sessionId,

        List<LeaderboardEntry> entries,

        int totalParticipants,

        long lastUpdated,

        String status // "LIVE", "FINAL", "CACHED"

) {

    @Builder
    public record LeaderboardEntry(

            String participantId,

            String nickname,

            int totalScore,

            int position,

            Long rank, // Current rank from Redis

            boolean isCurrentUser

    ) {}
}

