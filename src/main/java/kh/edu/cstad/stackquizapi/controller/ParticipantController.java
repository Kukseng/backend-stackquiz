package kh.edu.cstad.stackquizapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kh.edu.cstad.stackquizapi.dto.request.JoinSessionRequest;
import kh.edu.cstad.stackquizapi.dto.request.SubmitAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.response.ParticipantResponse;
import kh.edu.cstad.stackquizapi.dto.response.SubmitAnswerResponse;
import kh.edu.cstad.stackquizapi.service.ParticipantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/participants")

public class ParticipantController {

    private final ParticipantService participantService;

    /**
     * Join a quiz session
     * @param request Contains session code and participant nickname
     * @return ParticipantResponse with participant details
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/join")
    public ParticipantResponse joinSession(@Valid @RequestBody JoinSessionRequest request) {
        return participantService.joinSession(request);
    }

    @Operation(summary = "Join quiz session as authenticated user",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/join/auth-user")
    public ParticipantResponse joinSessionAsAuthenticatedUser(@Valid @AuthenticationPrincipal Jwt accessToken, JoinSessionRequest request) {
        return participantService.joinSessionAsAuthenticatedUser(accessToken, request);
    }

    /**
     * Submit an answer to a question
     * @param request Contains participant ID, question ID, selected option, and time taken
     * @return Response with answer submission details
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/submit-answer")
    public SubmitAnswerResponse submitAnswer(@Valid @RequestBody SubmitAnswerRequest request) {
        return participantService.submitAnswer(request);
    }

    /**
     * Get all active participants in a session
     * @return List of active participants
     */
    @Operation(summary = "Host get all participants in a session",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{quizCode}")
    public List<ParticipantResponse> getSessionParticipants(@PathVariable String quizCode) {
        return participantService.getSessionParticipants(quizCode);
    }

    /**
     * Remove participant from session (mark as inactive)
     * @param participantId The participant ID to remove
     */
    @Operation(summary = "Host Delete specify participants in a session",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{participantId}")
    public void leaveSession(@PathVariable String participantId) {
        participantService.leaveSession(participantId);
    }

    /**
     * Check if a nickname is available in a session
     * @param quizCode The session ID
     * @param nickname The nickname to check
     * @return Boolean indicating availability
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{quizCode}/nickname-available")
    public boolean isNicknameAvailable(
            @PathVariable String quizCode,
            @RequestParam String nickname) {
        return participantService.isNicknameAvailable(quizCode, nickname);
    }

    /**
     * Check if a session can be joined
     * @param quizCode The session code
     * @return Boolean indicating if session is joinable
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{quizCode}/can-join")
    public boolean canJoinSession(@PathVariable String quizCode) {
        return participantService.canJoinSession(quizCode);
    }
}