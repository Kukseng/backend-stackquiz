package kh.edu.cstad.stackquizapi.dto.response;

import kh.edu.cstad.stackquizapi.util.SatisfactionLevel;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CreateFeedbackResponse(

        String feedbackId,

        String userId,

        String quizId,

        SatisfactionLevel satisfactionLevel,

        String text,

        String status,

        String message,

        LocalDateTime createdAt

) {
}
