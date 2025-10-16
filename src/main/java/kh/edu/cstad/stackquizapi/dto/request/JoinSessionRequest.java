package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JoinSessionRequest(

        @NotBlank(message = "Session code is required")
        String quizCode,

        @NotBlank(message = "Nickname is required")
        @Size(min = 2, max = 20, message = "Nickname must be between 2 and 20 characters")
        String nickname,

        @NotNull(message = "Avatar ID is required")
        @Min(value = 1, message = "Avatar ID must be greater than 0")
        Long avatarId

) {

}