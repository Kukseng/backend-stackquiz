package kh.edu.cstad.stackquizapi.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record UserProfileResponse(

        String userId,

        String username,

        String email,

        String firstName,

        String lastName,

        boolean emailVerified,

        boolean enabled,

        List<String> roles,

        Long createdTimestamp

) {
}
