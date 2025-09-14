package kh.edu.cstad.stackquizapi.dto.request;

public record OAuthRegisterRequest(

        String email,

        String firstName,

        String lastName,

        String username
) {
}