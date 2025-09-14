package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateAvatarRequest(

        @NotBlank(message = "Avatar name is required")
        String avatarName

) {
}
