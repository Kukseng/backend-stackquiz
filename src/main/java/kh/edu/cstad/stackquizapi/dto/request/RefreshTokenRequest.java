package kh.edu.cstad.stackquizapi.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RefreshTokenRequest(

        @JsonProperty("refresh_token")
        String refreshToken

) {
}
