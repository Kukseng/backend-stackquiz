package kh.edu.cstad.stackquizapi.dto.request;

public record QuizUpdateRequest(

        String title,

        String description,

        String thumbnailUrl,

        String visibility

) {
}
