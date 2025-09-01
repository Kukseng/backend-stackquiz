package kh.edu.cstad.stackquizapi.dto.response;

import lombok.Builder;

// For session statistics
@Builder
public record SessionStats(

        int totalQuestions,

        double averageScore,

        double completionRate,

        String duration,

        int totalParticipants,

        double highestScore,

        double lowestScore

) {}
