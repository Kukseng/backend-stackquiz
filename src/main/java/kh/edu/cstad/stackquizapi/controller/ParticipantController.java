package kh.edu.cstad.stackquizapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kh.edu.cstad.stackquizapi.dto.request.JoinSessionRequest;
import kh.edu.cstad.stackquizapi.dto.request.SubmitAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.response.ParticipantResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuestionAnalyticsResponse;
import kh.edu.cstad.stackquizapi.dto.response.SubmitAnswerResponse;
import kh.edu.cstad.stackquizapi.service.HostDashboardService;
import kh.edu.cstad.stackquizapi.service.ParticipantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/participants")
public class ParticipantController {

    private final ParticipantService participantService;
    private final HostDashboardService hostDashboardService; // ✅ ADDED: Inject HostDashboardService

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/join")
    public ParticipantResponse joinSession(@Valid @RequestBody JoinSessionRequest request) {
        return participantService.joinSession(request);
    }

    @Operation(summary = "Join quiz session as authenticated user",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/join/auth-user")
    public ParticipantResponse joinSessionAsAuthenticatedUser(@Valid @AuthenticationPrincipal Jwt accessToken,@Valid @RequestBody  JoinSessionRequest request) {
        return participantService.joinSessionAsAuthenticatedUser(accessToken, request);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/submit-answer")
    public SubmitAnswerResponse submitAnswer(@Valid @RequestBody SubmitAnswerRequest request) {
        return participantService.submitAnswer(request);
    }

    @Operation(summary = "Host get all participants in a session",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{quizCode}")
    public List<ParticipantResponse> getSessionParticipants(@PathVariable String quizCode) {
        return participantService.getSessionParticipants(quizCode);
    }

    @Operation(summary = "Host Delete specify participants in a session",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{participantId}")
    public void leaveSession(@PathVariable String participantId) {
        participantService.leaveSession(participantId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{quizCode}/nickname-available")
    public boolean isNicknameAvailable(
            @PathVariable String quizCode,
            @RequestParam String nickname) {
        return participantService.isNicknameAvailable(quizCode, nickname);
    }

    @Operation(summary = "Get question analytics for participants (no auth required)")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionCode}/question-analytics")
    public ResponseEntity<QuestionAnalyticsResponse> getQuestionAnalyticsForParticipant(
            @PathVariable String sessionCode) {
        log.info("GET /api/v1/participants/session/{}/question-analytics", sessionCode);

        // FIXED: Use the existing HostDashboardService method
        // This is safe because the analytics data is public to all participants in the session
        QuestionAnalyticsResponse analytics = hostDashboardService.getQuestionAnalytics(sessionCode);

        return ResponseEntity.ok(analytics);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{quizCode}/can-join")
    public boolean canJoinSession(@PathVariable String quizCode) {
        return participantService.canJoinSession(quizCode);
    }
}

