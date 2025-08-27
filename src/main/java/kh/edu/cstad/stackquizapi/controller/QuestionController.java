package kh.edu.cstad.stackquizapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import kh.edu.cstad.stackquizapi.dto.request.CreateQuestionRequest;
import kh.edu.cstad.stackquizapi.dto.request.UpdateQuestionRequest;
import kh.edu.cstad.stackquizapi.dto.response.QuestionResponse;
import kh.edu.cstad.stackquizapi.service.QuestionService;
import kh.edu.cstad.stackquizapi.util.QuestionType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "Create new question")
    @PostMapping
    public ResponseEntity<QuestionResponse> createQuestion(@RequestBody CreateQuestionRequest request) {
        QuestionResponse response = questionService.createNewQuestion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all questions")
    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getAllQuestions() {
        List<QuestionResponse> responses = questionService.getAllQuestions();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get question by ID")
    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponse> getQuestionById(@PathVariable String id) {
        QuestionResponse response = questionService.getQuestionById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Partially update question by ID")
    @PatchMapping("/{id}")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable String id,
            @RequestBody UpdateQuestionRequest updateQuestionRequest) {
        QuestionResponse response = questionService.updateQuestionById(id, updateQuestionRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete question by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestionById(@PathVariable String id) {
        questionService.deleteQuestionById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete question by list of IDs")
    @DeleteMapping
    public ResponseEntity<Void> deleteQuestionsByIds(@RequestBody List<String> ids) {
        questionService.deleteQuestionsByIds(ids);
        return ResponseEntity.noContent().build();
    }

}
