package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitAnswerRequest(

        @NotBlank(message = "Participant ID is required")
        String participantId,

        @NotBlank(message = "Question ID is required")
        String questionId,

        String optionId,

        String answerText,

        @NotNull(message = "Time taken is required")
        @Min(value = 0, message = "Time taken must be non-negative")
        Integer timeTaken,

        String sessionId

) {}