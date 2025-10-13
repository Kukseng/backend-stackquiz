package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record LeaderboardRequest(

        @NotBlank(message = "Session ID is required")
        String sessionId,

        @Min(value = 1, message = "Limit must be at least 1")
        @Max(value = 100, message = "Limit cannot exceed 100")
        Integer limit,

        @Min(value = 0, message = "Offset cannot be negative")
        Integer offset,

        Boolean includeInactive,

        String currentParticipantId
) {

    public LeaderboardRequest {
        if (limit == null) limit = 20;
        if (offset == null) offset = 0;
        if (includeInactive == null) includeInactive = false;
    }
}



