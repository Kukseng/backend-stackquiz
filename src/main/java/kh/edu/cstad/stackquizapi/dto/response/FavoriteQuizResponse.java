package kh.edu.cstad.stackquizapi.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FavoriteQuizResponse(

        String id,

        String quizId,

        String username,

        LocalDateTime createdAt

) {
}
