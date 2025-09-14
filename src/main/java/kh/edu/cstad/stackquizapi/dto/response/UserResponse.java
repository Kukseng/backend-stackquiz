package kh.edu.cstad.stackquizapi.dto.response;

import java.time.LocalDateTime;

public record UserResponse(

        String id,

        String profileUser,

        String username,

        String email,

        String firstName,

        String lastName,

        String avatarUrl,

        boolean isActive,

        LocalDateTime createdAt

) {
}