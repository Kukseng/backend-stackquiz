package kh.edu.cstad.stackquizapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kh.edu.cstad.stackquizapi.dto.request.CreateQuizRequest;
import kh.edu.cstad.stackquizapi.dto.request.QuizUpdate;
import kh.edu.cstad.stackquizapi.dto.response.QuizResponse;
import kh.edu.cstad.stackquizapi.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/quizzes")
public class QuizController {
    /***
     * Author : Kukseng
     * Handle : Quiz Management (Similar to Kahoot style)
     */
    private final QuizService quizService;

    /**
     * Create a new quiz
     */
    // get current user for profile
    @Operation(summary = "Get all users (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public QuizResponse createQuiz(@RequestBody CreateQuizRequest createquizRequest) {
        return quizService.createQuiz(createquizRequest);
    }

    /**
     * Update quiz by quizId
     */
    // get current user for profile
    @Operation(summary = "Get all users (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{quizId}")
    public QuizResponse updateQuiz(@PathVariable String quizId,
                                   @RequestBody QuizUpdate quizUpdate) {
        return quizService.updateQuiz(quizId, quizUpdate);
    }

    /**
     * Get all quizzes (optionally filter by active status)
     */
    // get current user for profile
    @Operation(summary = "Get all users (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<QuizResponse> getAllQuizzes(@RequestParam(defaultValue = "true") boolean active) {
        return quizService.getAllQuiz(active);
    }

    /**
     * Get a single quiz by quizId
     */
    // get current user for profile
    @Operation(summary = "Get all users (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{quizId}")
    public QuizResponse getQuizById(@PathVariable String quizId) {
        return quizService.getQuizById(quizId);
    }

    /**
     * Delete quiz by quizId
     */
    // get current user for profile
    @Operation(summary = "Get all users (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{quizId}")
    public void deleteQuiz(@PathVariable String quizId) {
        quizService.deleteQuiz(quizId);
    }

    /**
     * Get all quizzes created by a specific user
     */
    // get current user for profile
    @Operation(summary = "Get all users (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/users/{userId}")
    public List<QuizResponse> getQuizzesByUser(@PathVariable String userId) {
        return quizService.getQuizByUserId(userId);
    }
}