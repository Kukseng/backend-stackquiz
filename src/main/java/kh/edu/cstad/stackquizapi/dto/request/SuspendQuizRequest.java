package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SuspendQuizRequest(

        @NotBlank(message = "Reason is required")
        String reason

) {}
