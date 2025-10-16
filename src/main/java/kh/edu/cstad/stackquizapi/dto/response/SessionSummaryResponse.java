package kh.edu.cstad.stackquizapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionSummaryResponse {
    private String sessionId;
    private String sessionCode;
    private String sessionName;
    private String quizTitle;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalParticipants;
    private Double averageAccuracy;
    private Double completionRate;
    private String hostName;
    private Integer totalQuestions;
}

