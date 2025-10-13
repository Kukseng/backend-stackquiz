package kh.edu.cstad.stackquizapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import kh.edu.cstad.stackquizapi.dto.request.RatingRequest;
import kh.edu.cstad.stackquizapi.dto.response.RatingResponse;
import kh.edu.cstad.stackquizapi.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @Operation(summary = "Rate quiz with quizId (average user)",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{quizId}")
    public RatingResponse rateQuiz(@Valid @PathVariable String quizId,
                                   @RequestBody RatingRequest ratingRequest,
                                   @AuthenticationPrincipal Jwt accessToken) {
        return ratingService.rateQuiz(quizId, ratingRequest, accessToken);
    }

    @Operation(summary = "Get rating by quizId (average user)",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/{quizId}")
    public List<RatingResponse> getRatingsByQuiz(@PathVariable String quizId) {
        return ratingService.getRatingsByQuiz(quizId);
    }

    @GetMapping("/me")
    public List<RatingResponse> getRatingsByUser(
            @AuthenticationPrincipal Jwt accessToken) {
        return ratingService.getRatingsByUser(accessToken);
    }

    @Operation(
            summary = "Get average rating for a quiz",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @GetMapping("/{quizId}/average")
    public double getAverageRating(@PathVariable String quizId) {
        return ratingService.getAverageRating(quizId);
    }

    @Operation(
            summary = "Delete current user's rating for a quiz",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{quizId}")
    public void deleteRating(
            @PathVariable String quizId,
            @AuthenticationPrincipal Jwt accessToken
    ) {
        ratingService.deleteRating(quizId, accessToken);
    }

}
