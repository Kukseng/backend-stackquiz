package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record QuizReportRequest(

        @NotBlank(message = "Reason is required")
        String reason,

        String description

) {}
