package kh.edu.cstad.stackquizapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kh.edu.cstad.stackquizapi.dto.request.AIQuizGenerationRequest;
import kh.edu.cstad.stackquizapi.dto.response.AIQuizGenerationResponse;
import kh.edu.cstad.stackquizapi.service.AIQuizGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai/quiz")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Quiz Generation", description = "AI-powered quiz question generation endpoints")
public class AIQuizGenerationController {

    private final AIQuizGenerationService aiQuizGenerationService;

    @PostMapping("/generate")
    @Operation(summary = "Generate quiz questions using AI", 
               description = "Generate multiple quiz questions with options based on topic and parameters using AI")
    public ResponseEntity<Map<String, Object>> generateQuestions(
            @Valid @RequestBody AIQuizGenerationRequest request) {
        
        log.info("Received AI generation request for topic: {}", request.getTopic());
        
        try {
            AIQuizGenerationResponse response = aiQuizGenerationService.generateQuestions(request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Questions generated successfully");
            result.put("data", response);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error generating questions", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to generate questions: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/generate/single")
    @Operation(summary = "Generate a single question", 
               description = "Generate one quiz question with options")
    public ResponseEntity<Map<String, Object>> generateSingleQuestion(
            @RequestParam String topic,
            @RequestParam(defaultValue = "MEDIUM") String difficulty,
            @RequestParam(defaultValue = "MULTIPLE_CHOICE") String questionType) {
        
        log.info("Generating single question for topic: {}", topic);
        
        try {
            AIQuizGenerationResponse.GeneratedQuestion question = 
                aiQuizGenerationService.generateSingleQuestion(topic, difficulty, questionType);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Question generated successfully");
            result.put("data", question);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error generating single question", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to generate question: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/improve")
    @Operation(summary = "Improve existing question", 
               description = "Use AI to improve and refine an existing question")
    public ResponseEntity<Map<String, Object>> improveQuestion(
            @RequestParam String questionText,
            @RequestParam(required = false) String context) {
        
        log.info("Improving question: {}", questionText);
        
        try {
            String improvedQuestion = aiQuizGenerationService.improveQuestion(questionText, context);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Question improved successfully");
            result.put("data", Map.of(
                "original", questionText,
                "improved", improvedQuestion
            ));
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error improving question", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to improve question: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Check AI service health", 
               description = "Verify that the AI generation service is available")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("service", "AI Quiz Generation");
        health.put("status", "operational");
        health.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(health);
    }
}

