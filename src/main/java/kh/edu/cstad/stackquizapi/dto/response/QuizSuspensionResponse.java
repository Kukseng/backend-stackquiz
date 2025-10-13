package kh.edu.cstad.stackquizapi.dto.response;

import java.time.LocalDateTime;

public record QuizSuspensionResponse(

        String status,

        String action,

        QuizInfo quiz,

        String reason,

        AdminInfo performedBy,

        LocalDateTime timestamp,

        NextSteps nextSteps,

        String message

) {
    public record QuizInfo(

            String id,

            String title,

            String previousStatus,

            String currentStatus,

            Boolean isActive,

            Boolean flagged

    ) {
    }

    public record AdminInfo(

            String adminId,

            String role

    ) {
    }

    public record NextSteps(

            String appealUrl,

            LocalDateTime canReinstateAfter

    ) {
    }
}

