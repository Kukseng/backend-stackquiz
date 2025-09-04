package kh.edu.cstad.stackquizapi.controller;

import kh.edu.cstad.stackquizapi.dto.request.CreateQuizRequest;
import kh.edu.cstad.stackquizapi.dto.request.QuizUpdate;
import kh.edu.cstad.stackquizapi.dto.response.QuizResponse;
import kh.edu.cstad.stackquizapi.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/quizzes")
public class QuizController {

    private final QuizService quizService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public QuizResponse createQuiz(@RequestBody CreateQuizRequest createQuizRequest,
                                   @AuthenticationPrincipal Jwt accessToken) {
        return quizService.createQuiz(createQuizRequest, accessToken);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{quizId}")
    public QuizResponse updateQuiz(@PathVariable String quizId,
                                   @RequestBody QuizUpdate quizUpdate,
                                   @AuthenticationPrincipal Jwt accessToken) {
        return quizService.updateQuiz(quizId, quizUpdate, accessToken);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<QuizResponse> getAllQuizzes(@RequestParam(defaultValue = "true") boolean active) {
        return quizService.getAllQuiz(active);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{quizId}")
    public QuizResponse getQuizById(@PathVariable String quizId) {
        return quizService.getQuizById(quizId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{quizId}")
    public void deleteQuiz(@PathVariable String quizId) {
        quizService.deleteQuiz(quizId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/users/me")
    public List<QuizResponse> getQuizzesByUser(@AuthenticationPrincipal Jwt accessToken) {
        return quizService.getQuizByUser(accessToken);
    }
}
