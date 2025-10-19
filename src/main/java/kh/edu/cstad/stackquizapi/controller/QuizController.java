package kh.edu.cstad.stackquizapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import kh.edu.cstad.stackquizapi.dto.request.CreateFeedbackRequest;
import kh.edu.cstad.stackquizapi.dto.request.CreateQuizRequest;
import kh.edu.cstad.stackquizapi.dto.request.FolkQuizRequest;
import kh.edu.cstad.stackquizapi.dto.request.QuizUpdateRequest;
import kh.edu.cstad.stackquizapi.dto.request.SuspendQuizRequest;
import kh.edu.cstad.stackquizapi.dto.response.FavoriteQuizResponse;
import kh.edu.cstad.stackquizapi.dto.response.CreateFeedbackResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuizFeedbackResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuizResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuizSuspensionResponse;
import kh.edu.cstad.stackquizapi.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/quizzes")
public class QuizController {

    private final QuizService quizService;

    @Operation(summary = "Create a new quiz", security = {@SecurityRequirement(name = "bearerAuth")})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public QuizResponse createQuiz(
            @Valid @RequestBody CreateQuizRequest createQuizRequest,
            @AuthenticationPrincipal Jwt accessToken) {
        return quizService.createQuiz(createQuizRequest, accessToken);
    }

    @Operation(summary = "Update an existing quiz", security = {@SecurityRequirement(name = "bearerAuth")})
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{quizId}")
    public QuizResponse updateQuiz(@Valid @PathVariable String quizId,
                                   @RequestBody QuizUpdateRequest quizUpdateRequest,
                                   @AuthenticationPrincipal Jwt accessToken) {
        return quizService.updateQuiz(quizId, quizUpdateRequest, accessToken);
    }

    @Operation(summary = "Get all quizzes (public)")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<QuizResponse> getAllQuizzes(@RequestParam(defaultValue = "true") boolean active) {
        return quizService.getAllQuiz(active);
    }

    @Operation(summary = "Get a quiz by ID (public)")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{quizId}")
    public QuizResponse getQuizById(@PathVariable String quizId) {
        return quizService.getQuizById(quizId);
    }

    @Operation(summary = "Delete a quiz by ID", security = {@SecurityRequirement(name = "bearerAuth")})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{quizId}")
    public void deleteQuiz(@PathVariable String quizId,
                           @AuthenticationPrincipal Jwt accessToken) {
        quizService.deleteQuiz(quizId, accessToken);
    }

    @Operation(summary = "Get quizzes created by the authenticated user", security = {@SecurityRequirement(name = "bearerAuth")})
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/users/me")
    public List<QuizResponse> getQuizzesByUser(@AuthenticationPrincipal Jwt accessToken) {
        return quizService.getQuizByUser(accessToken);
    }

    @Operation(summary = "Suspend quiz (admin)",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/admin/{quizId}/suspends")
    public QuizSuspensionResponse suspendQuiz(
            @PathVariable String quizId,
            @Valid @RequestBody SuspendQuizRequest request,
            @AuthenticationPrincipal Jwt accessToken
    ) {
        return quizService.suspendQuiz(quizId, request, accessToken);
    }

    @Operation(summary = "Add quiz to favorites (user)",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @PostMapping("/{quizId}/favorite")
    public ResponseEntity<FavoriteQuizResponse> addToFavorite(
            @PathVariable String quizId,
            @AuthenticationPrincipal Jwt accessToken) {

        FavoriteQuizResponse response = quizService.atToFavorite(quizId, accessToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Remove quiz from favorites (user)",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @DeleteMapping("/{quizId}/favorite")
    public ResponseEntity<Void> removeFromFavorite(
            @PathVariable String quizId,
            @AuthenticationPrincipal Jwt accessToken) {

        quizService.removeFromFavorite(quizId, accessToken);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all favorite quizzes (admin)",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/favorite")
    public ResponseEntity<List<FavoriteQuizResponse>> getFavoriteQuizzes() {
        List<FavoriteQuizResponse> response = quizService.getFavoriteQuizzes();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get current user favorite quizzes (current organizer)",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/favorite/me")
    public ResponseEntity<List<FavoriteQuizResponse>> getCurrentUserFavoriteQuizzes(@AuthenticationPrincipal Jwt accessToken) {
        List<FavoriteQuizResponse> response = quizService.getCurrentUserFavoriteQuizzes(accessToken);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Fork an existing quiz",
            description = "Create a new quiz based on an existing one. The forked quiz copies all questions and metadata from the original quiz, and assigns the current user as the new author.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{quizId}/fork")
    public QuizResponse folkQuiz(@AuthenticationPrincipal Jwt accessToken,
                                 @PathVariable String quizId,
                                 @RequestBody FolkQuizRequest folkQuizRequest) {
        return quizService.folkQuiz(accessToken, quizId, folkQuizRequest);
    }

    @Operation(
            summary = "Feedback quizzes",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{quizId}/feedback")
    public CreateFeedbackResponse giveFeedback(@Valid @RequestBody CreateFeedbackRequest createFeedbackRequest,
                                               @PathVariable String quizId,
                                               @AuthenticationPrincipal Jwt accessToken) {
        return quizService.giveFeedback(createFeedbackRequest, quizId, accessToken);
    }

    @Operation(
            summary = "Get all feedbacks from participants (admin)",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @GetMapping("/feedback")
    List<QuizFeedbackResponse> getAllFeedbacks() {
        return quizService.getAllFeedbacks();
    }

    @Operation(
            summary = "Get all current-user quizzes' feedbacks from participants (user)",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @GetMapping("/feedback/me")
    public List<QuizFeedbackResponse> getCurrentUserQuizFeedbacks(@AuthenticationPrincipal Jwt accessToken) {
        return quizService.getCurrentUserQuizFeedbacks(accessToken);
    }

}
