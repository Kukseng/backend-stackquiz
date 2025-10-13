package kh.edu.cstad.stackquizapi.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record LoginResponse(

        @JsonProperty("access_token") String accessToken,

        @JsonProperty("refresh_token") String refreshToken,

        @JsonProperty("expires_in") int expiresIn,

        @JsonProperty("refresh_expires_in") int refreshExpiresIn,

        @JsonProperty("token_type") String tokenType,

        @JsonProperty("scope") String scope

) {}
