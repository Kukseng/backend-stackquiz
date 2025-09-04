package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;


public record HistoricalLeaderboardRequest(

        String sessionId,

        java.time.LocalDateTime startDate,

        java.time.LocalDateTime endDate,

        @Min(value = 1, message = "Page must be at least 1")
        Integer page,

        @Min(value = 1, message = "Size must be at least 1")
        @Max(value = 50, message = "Size cannot exceed 50")
        Integer size
) {
    public HistoricalLeaderboardRequest {
        if (page == null) page = 1;
        if (size == null) size = 10;
    }
}