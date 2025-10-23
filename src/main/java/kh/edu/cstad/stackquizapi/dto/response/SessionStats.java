package kh.edu.cstad.stackquizapi.dto.response;

import lombok.Builder;

@Builder
public record SessionStats(

        Integer totalQuestions,

        Double averageScore,

        Double completionRate,

        String duration,

        Integer totalParticipants,

        Double highestScore,

        Double lowestScore,

        Integer totalAnswers,

        Integer correctAnswers,

        Double accuracyRate

) {
}