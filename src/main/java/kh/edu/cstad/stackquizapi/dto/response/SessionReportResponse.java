package kh.edu.cstad.stackquizapi.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SessionReportResponse(

        String reportId,

        String sessionId,

        String sessionName,

        String hostName,

        LocalDateTime generatedAt,

        JsonNode sessionOverview,

        JsonNode questionBreakdown,

        JsonNode participantDetails,

        JsonNode finalRankings

) {}