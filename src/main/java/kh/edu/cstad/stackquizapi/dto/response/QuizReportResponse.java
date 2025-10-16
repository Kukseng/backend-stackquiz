package kh.edu.cstad.stackquizapi.dto.response;

import java.time.LocalDateTime;

public record QuizReportResponse(

        String id,

        String quizId,

        String userId,

        String reason,

        String description,

        LocalDateTime createdAt

) {}
