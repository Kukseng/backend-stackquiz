package kh.edu.cstad.stackquizapi.dto.response;

import lombok.Builder;

// Separate record for real-time updates
@Builder
public record LiveLeaderboardUpdate(
        String sessionId,

        String participantId,

        String nickname,

        int newScore,

        int newPosition,

        int previousPosition,

        long timestamp

) {}
