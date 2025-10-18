package kh.edu.cstad.stackquizapi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kh.edu.cstad.stackquizapi.dto.request.AIQuizGenerationRequest;
import kh.edu.cstad.stackquizapi.dto.response.AIQuizGenerationResponse;
import kh.edu.cstad.stackquizapi.service.AIQuizGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIQuizGenerationServiceImpl implements AIQuizGenerationService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${OPENAI_API_KEY:local}")
    private String apiKey;
    
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4.1-mini"; // Using the available model

    @Override
    public AIQuizGenerationResponse generateQuestions(AIQuizGenerationRequest request) {
        log.info("Generating {} questions for topic: {}", request.getNumberOfQuestions(), request.getTopic());
        
        String prompt = buildPrompt(request);
        String aiResponse = callOpenAI(prompt);
        
        List<AIQuizGenerationResponse.GeneratedQuestion> questions = parseAIResponse(aiResponse, request);
        
        return AIQuizGenerationResponse.builder()
                .topic(request.getTopic())
                .difficulty(request.getDifficulty())
                .totalQuestions(questions.size())
                .questions(questions)
                .generatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }

    @Override
    public AIQuizGenerationResponse.GeneratedQuestion generateSingleQuestion(
            String topic, String difficulty, String questionType) {
        
        AIQuizGenerationRequest request = new AIQuizGenerationRequest();
        request.setTopic(topic);
        request.setDifficulty(difficulty);
        request.setQuestionType(questionType);
        request.setNumberOfQuestions(1);
        request.setNumberOfOptions(4);
        request.setTimeLimit(30);
        request.setPoints(100);
        
        AIQuizGenerationResponse response = generateQuestions(request);
        return response.getQuestions().isEmpty() ? null : response.getQuestions().get(0);
    }

    @Override
    public String improveQuestion(String questionText, String context) {
        String prompt = String.format(
            "Improve the following quiz question to make it clearer and more engaging:\\n\\n" +
            "Original Question: %s\\n\\n" +
            "Context/Requirements: %s\\n\\n" +
            "Provide only the improved question text, nothing else.",
            questionText, context != null ? context : "Make it more clear and educational"
        );
        
        return callOpenAI(prompt);
    }

    private String buildPrompt(AIQuizGenerationRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert quiz creator. Generate ")
              .append(request.getNumberOfQuestions())
              .append(" quiz question(s) about: ").append(request.getTopic()).append("\\n\\n");
        
        prompt.append("Requirements:\\n");
        prompt.append("- Difficulty: ").append(request.getDifficulty()).append("\\n");
        prompt.append("- Question Type: ").append(request.getQuestionType()).append("\\n");
        prompt.append("- Number of options per question: ").append(request.getNumberOfOptions()).append("\\n");
        prompt.append("- Language: ").append(request.getLanguage()).append("\\n");
        
        if (request.getAdditionalContext() != null && !request.getAdditionalContext().isEmpty()) {
            prompt.append("- Additional Context: ").append(request.getAdditionalContext()).append("\\n");
        }
        
        prompt.append("\\nFormat your response as a JSON array with this exact structure:\\n");
        prompt.append("[\\n");
        prompt.append("  {\\n");
        prompt.append("    \\\"questionText\\\": \\\"The question text here\\\",\\n");
        prompt.append("    \\\"options\\\": [\\n");
        prompt.append("      {\\\"text\\\": \\\"Option A\\\", \\\"isCorrect\\\": true},\\n");
        prompt.append("      {\\\"text\\\": \\\"Option B\\\", \\\"isCorrect\\\": false},\\n");
        prompt.append("      {\\\"text\\\": \\\"Option C\\\", \\\"isCorrect\\\": false},\\n");
        prompt.append("      {\\\"text\\\": \\\"Option D\\\", \\\"isCorrect\\\": false}\\n");
        prompt.append("    ],\\n");
        
        if (request.getIncludeExplanations()) {
            prompt.append("    \\\"explanation\\\": \\\"Brief explanation of the correct answer\\\"\\n");
        }
        
        prompt.append("  }\\n");
        prompt.append("]\\n\\n");
        prompt.append("Provide ONLY the JSON array, no additional text or markdown formatting.");
        
        return prompt.toString();
    }

    private String callOpenAI(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);
            requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String response = restTemplate.postForObject(OPENAI_API_URL, entity, String.class);
            
            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            
            log.debug("AI Response: {}", content);
            return content;
            
        } catch (Exception e) {
            log.error("Error calling OpenAI API", e);
            throw new RuntimeException("Failed to generate questions using AI: " + e.getMessage(), e);
        }
    }

    private List<AIQuizGenerationResponse.GeneratedQuestion> parseAIResponse(
            String aiResponse, AIQuizGenerationRequest request) {
        
        List<AIQuizGenerationResponse.GeneratedQuestion> questions = new ArrayList<>();
        
        try {
            // Clean up the response - remove markdown code blocks if present
            String cleanedResponse = aiResponse.trim();
            if (cleanedResponse.startsWith("```json")) {
                cleanedResponse = cleanedResponse.substring(7);
            }
            if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse.substring(3);
            }
            if (cleanedResponse.endsWith("```")) {
                cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
            }
            cleanedResponse = cleanedResponse.trim();
            
            JsonNode questionsArray = objectMapper.readTree(cleanedResponse);
            
            if (!questionsArray.isArray()) {
                throw new RuntimeException("AI response is not a valid JSON array");
            }
            
            for (JsonNode questionNode : questionsArray) {
                AIQuizGenerationResponse.GeneratedQuestion question = parseQuestion(questionNode, request);
                questions.add(question);
            }
            
        } catch (JsonProcessingException e) {
            log.error("Error parsing AI response", e);
            throw new RuntimeException("Failed to parse AI-generated questions: " + e.getMessage(), e);
        }
        
        return questions;
    }

    private AIQuizGenerationResponse.GeneratedQuestion parseQuestion(
            JsonNode questionNode, AIQuizGenerationRequest request) {
        
        List<AIQuizGenerationResponse.GeneratedOption> options = new ArrayList<>();
        JsonNode optionsArray = questionNode.path("options");
        
        int order = 1;
        for (JsonNode optionNode : optionsArray) {
            AIQuizGenerationResponse.GeneratedOption option = AIQuizGenerationResponse.GeneratedOption.builder()
                    .optionText(optionNode.path("text").asText())
                    .isCorrect(optionNode.path("isCorrect").asBoolean(false))
                    .optionOrder(order++)
                    .build();
            options.add(option);
        }
        
        return AIQuizGenerationResponse.GeneratedQuestion.builder()
                .questionText(questionNode.path("questionText").asText())
                .questionType(request.getQuestionType())
                .timeLimit(request.getTimeLimit())
                .points(request.getPoints())
                .options(options)
                .explanation(questionNode.path("explanation").asText(null))
                .build();
    }
}

