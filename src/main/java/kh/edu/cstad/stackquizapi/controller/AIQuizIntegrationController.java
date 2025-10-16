package kh.edu.cstad.stackquizapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kh.edu.cstad.stackquizapi.domain.Question;
import kh.edu.cstad.stackquizapi.dto.request.AIQuizGenerationRequest;
import kh.edu.cstad.stackquizapi.service.impl.AIQuizIntegrationServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/quiz/{quizId}/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Quiz Integration", description = "Generate and add AI questions directly to quizzes")
public class AIQuizIntegrationController {

    private final AIQuizIntegrationServiceImpl aiQuizIntegrationService;

    @PostMapping("/generate-questions")
    @Operation(summary = "Generate and add AI questions to quiz", 
               description = "Generate multiple questions using AI and automatically add them to the specified quiz")
    public ResponseEntity<Map<String, Object>> generateAndAddQuestions(
            @PathVariable String quizId,
            @Valid @RequestBody AIQuizGenerationRequest request) {
        
        log.info("Generating and adding AI questions to quiz: {}", quizId);
        
        try {
            List<Question> questions = aiQuizIntegrationService.generateAndSaveQuestions(quizId, request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", questions.size() + " questions generated and added successfully");
            result.put("data", Map.of(
                "quizId", quizId,
                "questionsAdded", questions.size(),
                "questions", questions
            ));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
            
        } catch (Exception e) {
            log.error("Error generating and adding questions to quiz", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to generate and add questions: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/generate-single")
    @Operation(summary = "Generate and add single AI question", 
               description = "Generate one question using AI and add it to the quiz")
    public ResponseEntity<Map<String, Object>> generateAndAddSingleQuestion(
            @PathVariable String quizId,
            @RequestParam String topic,
            @RequestParam(defaultValue = "MEDIUM") String difficulty,
            @RequestParam(defaultValue = "MULTIPLE_CHOICE") String questionType) {
        
        log.info("Generating single AI question for quiz: {}", quizId);
        
        try {
            Question question = aiQuizIntegrationService.generateAndSaveSingleQuestion(
                quizId, topic, difficulty, questionType
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Question generated and added successfully");
            result.put("data", question);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
            
        } catch (Exception e) {
            log.error("Error generating and adding single question", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to generate and add question: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

