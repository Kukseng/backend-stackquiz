package kh.edu.cstad.stackquizapi.dto.response;

import jakarta.validation.constraints.NotBlank;

public record AnswerResponse(

        String userId,

        String result,

        Integer pointAwarded,

        Integer totalScore

) {
}
