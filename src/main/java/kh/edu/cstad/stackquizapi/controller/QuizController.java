package kh.edu.cstad.stackquizapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import kh.edu.cstad.stackquizapi.dto.request.CreateQuizRequest;
import kh.edu.cstad.stackquizapi.dto.request.FolkQuizRequest;
import kh.edu.cstad.stackquizapi.dto.request.QuizUpdate;
import kh.edu.cstad.stackquizapi.dto.request.SuspendQuizRequest;
import kh.edu.cstad.stackquizapi.dto.response.AtToFavoriteResponse;
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

    @Operation(summary = "Create a new quiz", security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public QuizResponse createQuiz(
            @Valid @RequestBody CreateQuizRequest createQuizRequest,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt accessToken) {
        return quizService.createQuiz(createQuizRequest, file, accessToken);
    }

    @Operation(summary = "Update an existing quiz", security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{quizId}")
    public QuizResponse updateQuiz(@Valid @PathVariable String quizId,
                                   @RequestBody QuizUpdate quizUpdate,
                                   @AuthenticationPrincipal Jwt accessToken) {
        return quizService.updateQuiz(quizId, quizUpdate, accessToken);
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

    @Operation(summary = "Delete a quiz by ID", security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{quizId}")
    public void deleteQuiz(@PathVariable String quizId,
                           @AuthenticationPrincipal Jwt accessToken) {
        quizService.deleteQuiz(quizId, accessToken);
    }

    @Operation(summary = "Get quizzes created by the authenticated user", security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/users/me")
    public List<QuizResponse> getQuizzesByUser(@AuthenticationPrincipal Jwt accessToken) {
        return quizService.getQuizByUser(accessToken);
    }

    @Operation(summary = "Get quizzes created by the authenticated user (alias)", security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/me")
    public List<QuizResponse> getMyQuizzes(@AuthenticationPrincipal Jwt accessToken) {
        return quizService.getQuizByUser(accessToken);
    }

    @Operation(summary = "Suspend quiz (admin)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/admin/{quizId}/suspend")
    public ResponseEntity<QuizSuspensionResponse> suspendQuiz(
            @PathVariable String quizId,
            @Valid @RequestBody SuspendQuizRequest request,
            @AuthenticationPrincipal Jwt accessToken
    ) {
        QuizSuspensionResponse response = quizService.suspendQuiz(quizId, request, accessToken);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Add quiz to favorites (user)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @PostMapping("/{quizId}/favorite")
    public ResponseEntity<AtToFavoriteResponse> addToFavorite(
            @PathVariable String quizId,
            @AuthenticationPrincipal Jwt accessToken) {

        AtToFavoriteResponse response = quizService.atToFavorite(quizId, accessToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Remove quiz from favorites (user)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @DeleteMapping("/{quizId}/favorite")
    public ResponseEntity<Void> removeFromFavorite(
            @PathVariable String quizId,
            @AuthenticationPrincipal Jwt accessToken) {

        quizService.removeFromFavorite(quizId, accessToken);
        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "Fork an existing quiz",
            description = "Create a new quiz based on an existing one. The forked quiz copies all questions and metadata from the original quiz, and assigns the current user as the new author.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{quizId}/fork")
    public QuizResponse folkQuiz(@AuthenticationPrincipal Jwt accessToken,
                                 @PathVariable String quizId,
                                 @RequestBody FolkQuizRequest folkQuizRequest) {
        return quizService.folkQuiz(accessToken, quizId, folkQuizRequest);
    }


}
