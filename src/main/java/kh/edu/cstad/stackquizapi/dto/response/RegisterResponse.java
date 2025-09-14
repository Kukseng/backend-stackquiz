package kh.edu.cstad.stackquizapi.dto.response;

import lombok.Builder;

@Builder
public record RegisterResponse(

        String userId,

        String username,

        String email,

        String firstName,

        String lastName,

        String refreshToken,

        String accessToken,

        boolean emailVerified

) {
}