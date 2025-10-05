package kh.edu.cstad.stackquizapi.dto.response;

import lombok.Builder;

@Builder
public record RatingResponse(

        String id,

        String quizId,

        String userId,

        int stars,

        String comment,

        double averageRating

) {
}
