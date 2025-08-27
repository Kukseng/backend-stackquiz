package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record AnswerRequest(

        @NotBlank
        String userId,

        @NotBlank
        String questionId,

        String selectedOption

) {
}
