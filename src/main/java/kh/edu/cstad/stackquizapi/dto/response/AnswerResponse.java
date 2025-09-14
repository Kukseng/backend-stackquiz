package kh.edu.cstad.stackquizapi.dto.response;

public record AnswerResponse(

        String userId,

        String result,

        Integer pointAwarded,

        Integer totalScore

) {
}
