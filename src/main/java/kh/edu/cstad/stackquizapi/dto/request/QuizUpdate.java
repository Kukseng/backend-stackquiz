package kh.edu.cstad.stackquizapi.dto.request;

public record QuizUpdate(

        String title,

        String description,

        String thumbnailUrl,

        String visibility

) {
}
