package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AIQuizGenerationRequest {
    
    @NotBlank(message = "Topic is required")
    private String topic;
    
    @NotNull(message = "Number of questions is required")
    @Min(value = 1, message = "Must generate at least 1 question")
    @Max(value = 20, message = "Cannot generate more than 20 questions at once")
    private Integer numberOfQuestions;
    
    @NotBlank(message = "Difficulty level is required")
    private String difficulty; // EASY, MEDIUM, HARD
    
    @NotBlank(message = "Question type is required")
    private String questionType; // MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER
    
    private Integer numberOfOptions = 4; // Default 4 options for multiple choice
    
    private Integer timeLimit = 30; // Default 30 seconds
    
    private Integer points = 100; // Default 100 points
    
    private String language = "English"; // Default language
    
    private String additionalContext; // Optional additional instructions
    
    private Boolean includeExplanations = true; // Include answer explanations
}

