package kh.edu.cstad.stackquizapi.dto.response;

import java.time.LocalDateTime;

public record SessionResponse(

        String id,

        String sessionName,

        String sessionCode,

        String status,

        Integer currentQuestion,

        LocalDateTime startTime,

        LocalDateTime endTime,

        LocalDateTime createdAt,

        String quizTitle,

        Integer totalQuestions,

        String hostName,

        Integer participantCount

) {
}
