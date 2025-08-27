package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import kh.edu.cstad.stackquizapi.util.GenderUtil;


public record RegisterRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9_]{3,20}$", message = "Username must be 3–20 characters and contain only letters, numbers, or underscores")
        String username,

        @NotBlank
        @Email(message = "Invalid email format")
        String email,

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
        )
        String password,

        @NotBlank
        String confirmedPassword,

        @NotBlank
        @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ' -]{1,50}$", message = "First name contains invalid characters")
        String firstName,

        @NotBlank
        @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ' -]{1,50}$", message = "Last name contains invalid characters")
        String lastName

) {}