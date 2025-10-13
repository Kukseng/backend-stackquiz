package kh.edu.cstad.stackquizapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIQuizGenerationResponse {
    
    private String topic;
    private String difficulty;
    private Integer totalQuestions;
    private List<GeneratedQuestion> questions;
    private String generatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneratedQuestion {
        private String questionText;
        private String questionType;
        private Integer timeLimit;
        private Integer points;
        private List<GeneratedOption> options;
        private String explanation;
        private String imageUrl; // Optional AI-generated image URL
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneratedOption {
        private String optionText;
        private Boolean isCorrect;
        private Integer optionOrder;
    }
}

