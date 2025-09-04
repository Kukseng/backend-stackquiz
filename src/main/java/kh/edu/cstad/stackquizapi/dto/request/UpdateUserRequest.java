package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @Size(min = 3, max = 50, message = "Full name must be between 3 and 50 characters")
        String username,

        @Email(message = "Invalid email format")
        String email,

        @Size(min = 3, max = 50, message = "Full name must be between 3 and 50 characters")
        String firstName,

        @Size(min = 3, max = 50, message = "Full name must be between 3 and 50 characters")
        String lastName,

        String avatarUrl,

        String profileUser

){
}