package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateAvatarRequest(

        Integer avatarNo,

        @NotBlank(message = "Avatar name is required")
        String name

) {
}