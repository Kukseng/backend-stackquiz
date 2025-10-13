package kh.edu.cstad.stackquizapi.dto.response;

import lombok.Builder;

@Builder
public record AtToFavoriteResponse(

        String id,

        String quizId,

        String username,

        String createdAt

) {
}
