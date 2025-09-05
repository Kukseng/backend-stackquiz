package kh.edu.cstad.stackquizapi.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kh.edu.cstad.stackquizapi.dto.request.LeaderboardRequest;
import kh.edu.cstad.stackquizapi.dto.request.HistoricalLeaderboardRequest;
import kh.edu.cstad.stackquizapi.dto.response.*;
import kh.edu.cstad.stackquizapi.service.LeaderboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    // ----- Public endpoints -----

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/live")
    public LeaderboardResponse getRealTimeLeaderboard(@Valid @RequestBody LeaderboardRequest request) {
        return leaderboardService.getRealTimeLeaderboard(request);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}")
    public LeaderboardResponse getLeaderboard(@PathVariable String sessionId) {
        LeaderboardRequest request = new LeaderboardRequest(sessionId, 20, 0, false, null);
        return leaderboardService.getRealTimeLeaderboard(request);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/top/{limit}")
    public LeaderboardResponse getTopLeaderboard(
            @PathVariable String sessionId,
            @PathVariable int limit,
            @RequestParam(required = false) String participantId) {
        LeaderboardRequest request = new LeaderboardRequest(sessionId, limit, 0, false, participantId);
        return leaderboardService.getRealTimeLeaderboard(request);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/podium")
    public LeaderboardResponse getPodium(@PathVariable String sessionId) {
        return leaderboardService.getPodium(sessionId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/participant/{participantId}/rank")
    public ParticipantRankResponse getParticipantRank(
            @PathVariable String sessionId,
            @PathVariable String participantId) {
        return leaderboardService.getParticipantRank(sessionId, participantId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/report")
    public HistoricalLeaderboardResponse getSessionReport(@PathVariable String sessionId) {
        return leaderboardService.getSessionReport(sessionId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/stats")
    public SessionStats getSessionStats(@PathVariable String sessionId) {
        return leaderboardService.getSessionStatistics(sessionId);
    }

    // ----- Secured endpoints (for host/organizer) -----

    @Operation(summary = "Get historical leaderboards", security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/history")
    public List<HistoricalLeaderboardResponse> getHistoricalLeaderboards(
            @Valid @RequestBody HistoricalLeaderboardRequest request,
            @AuthenticationPrincipal Jwt accessToken) {
        return leaderboardService.getHistoricalLeaderboards(request, accessToken);
    }

    @Operation(summary = "Initialize leaderboard for a session", security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/session/{sessionId}/initialize")
    public void initializeLeaderboard(@PathVariable String sessionId) {
        leaderboardService.initializeSessionLeaderboard(sessionId);
    }

    @Operation(summary = "Finalize leaderboard when session ends", security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/session/{sessionId}/finalize")
    public void finalizeLeaderboard(@PathVariable String sessionId) {
        leaderboardService.finalizeSessionLeaderboard(sessionId);
    }
}
