package kh.edu.cstad.stackquizapi.dto.response;

import kh.edu.cstad.stackquizapi.util.SatisfactionLevel;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record QuizFeedbackResponse(

        String feedbackId,

        String userId,

        String quizId,

        SatisfactionLevel satisfactionLevel,

        String text,

        LocalDateTime createdAt

) {
}
