package kh.edu.cstad.stackquizapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import kh.edu.cstad.stackquizapi.dto.request.CreateQuestionRequest;
import kh.edu.cstad.stackquizapi.dto.request.UpdateQuestionRequest;
import kh.edu.cstad.stackquizapi.dto.response.QuestionResponse;
import kh.edu.cstad.stackquizapi.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "Create new question (users)",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(
            @RequestPart("data") @Valid CreateQuestionRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        QuestionResponse response = questionService.createNewQuestion(request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all questions (users)",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getAllQuestions() {
        List<QuestionResponse> responses = questionService.getAllQuestions();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get all questions (self)",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/me")
    public ResponseEntity<List<QuestionResponse>> getCurrentUserQuestions(@AuthenticationPrincipal Jwt accessToken) {
        List<QuestionResponse> responses = questionService.getCurrentUserQuestions(accessToken);
        return ResponseEntity.ok(responses);
    }


    @Operation(summary = "Get question by question ID (user)",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponse> getQuestionById(@PathVariable String id) {
        QuestionResponse response = questionService.getQuestionById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Partially update question by ID (user)")
    @PatchMapping("/{id}")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @Valid
            @PathVariable String id,
            @RequestBody UpdateQuestionRequest updateQuestionRequest) {
        QuestionResponse response = questionService.updateQuestionById(id, updateQuestionRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete single question by ID (user)",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestionById(@PathVariable String id) {
        questionService.deleteQuestionById(id);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Delete question by question IDs (user)",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @DeleteMapping
    public ResponseEntity<Void> deleteQuestionsByIds(@RequestBody List<String> ids) {
        questionService.deleteQuestionsByIds(ids);
        return ResponseEntity.noContent().build();
    }

}
