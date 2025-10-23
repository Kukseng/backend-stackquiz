package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LeaderboardSubscriptionRequest(

        @NotBlank(message = "Session ID is required")
        String sessionId,

        @NotBlank(message = "Participant ID is required")
        String participantId,

        Boolean subscribeToPodium,

        Boolean subscribeToPersonalRank
) {
    public LeaderboardSubscriptionRequest {
        if (subscribeToPodium == null) subscribeToPodium = true;
        if (subscribeToPersonalRank == null) subscribeToPersonalRank = true;
    }
}