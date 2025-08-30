package kh.edu.cstad.stackquizapi.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record LoginResponse(

        String accessToken,

        String refreshToken,

        String tokenType,

        Long expiresIn,

        Long refreshExpiresIn,

        String userId,

        String username,

        String email,

        String firstName,

        String lastName,

        List<String> roles,

        boolean emailVerified

) {
}
