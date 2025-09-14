package kh.edu.cstad.stackquizapi.dto.response;

// For getting participant's individual rank
public record ParticipantRankResponse(

        String participantId,

        String nickname,

        int currentScore,

        Long currentRank,

        int totalParticipants,

        String sessionId,

        long lastUpdated

) {}
