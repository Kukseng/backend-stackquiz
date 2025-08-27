package kh.edu.cstad.stackquizapi.dto.request;

public record SessionCreateRequest(

        String quizId,

        String hostId,

        String sessionName

) {
}
