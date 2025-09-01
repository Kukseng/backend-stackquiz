package kh.edu.cstad.stackquizapi.controller;


import kh.edu.cstad.stackquizapi.dto.request.LeaderboardRequest;
import kh.edu.cstad.stackquizapi.dto.request.HistoricalLeaderboardRequest;
import kh.edu.cstad.stackquizapi.dto.response.*;
import kh.edu.cstad.stackquizapi.service.LeaderboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/leaderboard")

public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    /**
     * Get real-time leaderboard with pagination and filters
     */
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/live")
    public LeaderboardResponse getRealTimeLeaderboard(@Valid @RequestBody LeaderboardRequest request) {
        return leaderboardService.getRealTimeLeaderboard(request);
    }

    /**
     * Get simple real-time leaderboard by session ID
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}")
    public LeaderboardResponse getLeaderboard(@PathVariable String sessionId) {
        LeaderboardRequest request = new LeaderboardRequest(sessionId, 20, 0, false, null);
        return leaderboardService.getRealTimeLeaderboard(request);
    }

    /**
     * Get top N participants
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/top/{limit}")
    public LeaderboardResponse getTopLeaderboard(
            @PathVariable String sessionId,
            @PathVariable int limit,
            @RequestParam(required = false) String participantId) {

        LeaderboardRequest request = new LeaderboardRequest(sessionId, limit, 0, false, participantId);
        return leaderboardService.getRealTimeLeaderboard(request);
    }

    /**
     * Get podium (top 3) for dramatic display
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/podium")
    public LeaderboardResponse getPodium(@PathVariable String sessionId) {
        return leaderboardService.getPodium(sessionId);
    }

    /**
     * Get specific participant's current rank and position
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/participant/{participantId}/rank")
    public ParticipantRankResponse getParticipantRank(
            @PathVariable String sessionId,
            @PathVariable String participantId) {
        return leaderboardService.getParticipantRank(sessionId, participantId);
    }

    /**
     * Get historical leaderboards (for host's past sessions)
     */
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/history")
    public List<HistoricalLeaderboardResponse> getHistoricalLeaderboards(
            @Valid @RequestBody HistoricalLeaderboardRequest request) {
        return leaderboardService.getHistoricalLeaderboards(request);
    }

    /**
     * Get comprehensive session report (final results)
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/report")
    public HistoricalLeaderboardResponse getSessionReport(@PathVariable String sessionId) {
        return leaderboardService.getSessionReport(sessionId);
    }

    /**
     * Get session statistics
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/stats")
    public SessionStats getSessionStats(@PathVariable String sessionId) {
        return leaderboardService.getSessionStatistics(sessionId);
    }

    /**
     * Initialize leaderboard for a new session
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/session/{sessionId}/initialize")
    public void initializeLeaderboard(@PathVariable String sessionId) {
        leaderboardService.initializeSessionLeaderboard(sessionId);
    }

    /**
     * Finalize leaderboard when session ends
     */
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/session/{sessionId}/finalize")
    public void finalizeLeaderboard(@PathVariable String sessionId) {
        leaderboardService.finalizeSessionLeaderboard(sessionId);
    }
}