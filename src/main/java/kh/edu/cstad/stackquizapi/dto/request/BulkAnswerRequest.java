package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkAnswerRequest(

        @NotBlank(message = "Participant ID is required")
        String participantId,

        @NotBlank(message = "Session ID is required")
        String sessionId,

        @NotEmpty(message = "Answers list cannot be empty")
        List<SingleAnswerRequest> answers
) {}
