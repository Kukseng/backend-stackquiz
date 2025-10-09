package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateUserRequest(

        @NotBlank(message = "User ID is required")
        String id,

        @NotBlank(message = "Full name is required")
        @Size(min = 3, max = 50, message = "Full name must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "Full name is required")
        @Size(min = 3, max = 50, message = "Full name must be between 3 and 50 characters")
        String firstName,

        @NotBlank(message = "Full name is required")
        @Size(min = 3, max = 50, message = "Full name must be between 3 and 50 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password,

        String avatarUrl,

        @NotBlank(message = "Is Deleted is required")
        Boolean isDeleted


) {
}