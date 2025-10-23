package kh.edu.cstad.stackquizapi.dto.response;

import kh.edu.cstad.stackquizapi.util.QuizMode;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SessionReportSummaryResponse(

        String sessionId,

        String sessionName,

        String quizTitle,

        String hostName,

        LocalDateTime startTime,

        LocalDateTime endTime,

        QuizMode mode,

        Integer totalQuestions,

        Integer totalParticipants,

        Double overallAccuracy,

        Double completionRate,

        Double averageScore,

        Integer totalAnswers,

        Integer correctAnswers

) {
}

