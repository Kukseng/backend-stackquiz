package kh.edu.cstad.stackquizapi.dto.request;

public record SessionCreateRequest(

        String quizId,

        String accessToken, // -> hostID

        String sessionName

) {
}
