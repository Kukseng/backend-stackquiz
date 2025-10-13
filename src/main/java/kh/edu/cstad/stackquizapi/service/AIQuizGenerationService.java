package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.AIQuizGenerationRequest;
import kh.edu.cstad.stackquizapi.dto.response.AIQuizGenerationResponse;

public interface AIQuizGenerationService {
    
    /**
     * Generate quiz questions using AI based on topic and parameters
     * @param request The generation request containing topic, difficulty, count, etc.
     * @return Generated questions with options
     */
    AIQuizGenerationResponse generateQuestions(AIQuizGenerationRequest request);
    
    /**
     * Generate a single question with options
     * @param topic The topic for the question
     * @param difficulty The difficulty level
     * @param questionType The type of question (MULTIPLE_CHOICE, TRUE_FALSE, etc.)
     * @return Generated question with options
     */
    AIQuizGenerationResponse.GeneratedQuestion generateSingleQuestion(
            String topic, 
            String difficulty, 
            String questionType
    );
    
    /**
     * Improve/refine an existing question using AI
     * @param questionText The original question text
     * @param context Additional context or requirements
     * @return Improved question
     */
    String improveQuestion(String questionText, String context);
}

