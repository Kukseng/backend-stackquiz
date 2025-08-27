package kh.edu.cstad.stackquizapi.controller;

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
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public QuizResponse createQuiz(@RequestBody CreateQuizRequest createquizRequest) {
        return quizService.createQuiz(createquizRequest);
    }

    /**
     * Update quiz by quizId
     */
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{quizId}")
    public QuizResponse updateQuiz(@PathVariable String quizId,
                                   @RequestBody QuizUpdate quizUpdate) {
        return quizService.updateQuiz(quizId, quizUpdate);
    }

    /**
     * Get all quizzes (optionally filter by active status)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<QuizResponse> getAllQuizzes(@RequestParam(defaultValue = "true") boolean active) {
        return quizService.getAllQuiz(active);
    }

    /**
     * Get a single quiz by quizId
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{quizId}")
    public QuizResponse getQuizById(@PathVariable String quizId) {
        return quizService.getQuizById(quizId);
    }

    /**
     * Delete quiz by quizId
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{quizId}")
    public void deleteQuiz(@PathVariable String quizId) {
        quizService.deleteQuiz(quizId);
    }

    /**
     * Get all quizzes created by a specific user
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/users/{userId}")
    public List<QuizResponse> getQuizzesByUser(@PathVariable String userId) {
        return quizService.getQuizByUserId(userId);
    }
}