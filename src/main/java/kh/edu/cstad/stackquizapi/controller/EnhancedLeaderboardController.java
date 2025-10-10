package kh.edu.cstad.stackquizapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kh.edu.cstad.stackquizapi.dto.response.LeaderboardResponse;
import kh.edu.cstad.stackquizapi.dto.websocket.ParticipantRankingMessage;
import kh.edu.cstad.stackquizapi.service.EnhancedLeaderboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/enhanced-leaderboard")
public class EnhancedLeaderboardController {

    private final EnhancedLeaderboardService enhancedLeaderboardService;

    @Operation(summary = "Get enhanced real-time leaderboard with context")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}")
    public LeaderboardResponse getEnhancedRealTimeLeaderboard(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return enhancedLeaderboardService.getEnhancedRealTimeLeaderboard(sessionId, limit, offset);
    }

    @Operation(summary = "Get leaderboard with current question context")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/question/{currentQuestion}")
    public LeaderboardResponse getLeaderboardWithQuestionContext(
            @PathVariable String sessionId,
            @PathVariable int currentQuestion) {
        return enhancedLeaderboardService.getLeaderboardWithQuestionContext(sessionId, currentQuestion);
    }

    @Operation(summary = "Get detailed participant ranking information")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/participant/{participantId}/ranking")
    public ParticipantRankingMessage getDetailedParticipantRanking(
            @PathVariable String sessionId,
            @PathVariable String participantId) {
        return enhancedLeaderboardService.getDetailedParticipantRanking(sessionId, participantId);
    }

    @Operation(summary = "Get top performers with rank change indicators")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/top-performers")
    public List<ParticipantRankingMessage> getTopPerformersWithChanges(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "5") int limit) {
        return enhancedLeaderboardService.getTopPerformersWithChanges(sessionId, limit);
    }

    @Operation(summary = "Get leaderboard segment around specific participant")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/participant/{participantId}/around")
    public LeaderboardResponse getLeaderboardAroundParticipant(
            @PathVariable String sessionId,
            @PathVariable String participantId,
            @RequestParam(defaultValue = "5") int range) {
        return enhancedLeaderboardService.getLeaderboardAroundParticipant(sessionId, participantId, range);
    }

    @Operation(summary = "Broadcast enhanced leaderboard updates",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/session/{sessionId}/broadcast")
    public void broadcastEnhancedLeaderboard(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "MANUAL_UPDATE") String updateType) {
        enhancedLeaderboardService.broadcastEnhancedLeaderboard(sessionId, updateType);
    }

    @Operation(summary = "Send personalized leaderboard to participant",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/session/{sessionId}/participant/{participantId}/personalized")
    public void sendPersonalizedLeaderboard(
            @PathVariable String sessionId,
            @PathVariable String participantId) {
        enhancedLeaderboardService.sendPersonalizedLeaderboard(sessionId, participantId);
    }

    @Operation(summary = "Get leaderboard with performance indicators")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/with-performance")
    public LeaderboardResponse getLeaderboardWithPerformance(@PathVariable String sessionId) {
        return enhancedLeaderboardService.getLeaderboardWithPerformance(sessionId);
    }

    @Operation(summary = "Get leaderboard changes since last update")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/changes")
    public Map<String, Object> getLeaderboardChanges(
            @PathVariable String sessionId,
            @RequestParam long lastUpdateTime) {
        return enhancedLeaderboardService.getLeaderboardChanges(sessionId, lastUpdateTime);
    }

    @Operation(summary = "Get leaderboard statistics for host view",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/statistics")
    public Map<String, Object> getLeaderboardStatistics(@PathVariable String sessionId) {
        return enhancedLeaderboardService.getLeaderboardStatistics(sessionId);
    }

    @Operation(summary = "Create leaderboard snapshot",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/session/{sessionId}/snapshot")
    public void createLeaderboardSnapshot(
            @PathVariable String sessionId,
            @RequestParam String snapshotType) {
        enhancedLeaderboardService.createLeaderboardSnapshot(sessionId, snapshotType);
    }

    @Operation(summary = "Get historical leaderboard snapshots",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/snapshots")
    public List<Map<String, Object>> getLeaderboardSnapshots(@PathVariable String sessionId) {
        return enhancedLeaderboardService.getLeaderboardSnapshots(sessionId);
    }

    @Operation(summary = "Get time-filtered leaderboard")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/time-filtered")
    public LeaderboardResponse getTimeFilteredLeaderboard(
            @PathVariable String sessionId,
            @RequestParam long startTime,
            @RequestParam long endTime) {
        return enhancedLeaderboardService.getTimeFilteredLeaderboard(sessionId, startTime, endTime);
    }

    @Operation(summary = "Get participant performance comparison")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/participant/{participantId}/comparison")
    public Map<String, Object> getParticipantComparison(
            @PathVariable String sessionId,
            @PathVariable String participantId) {
        return enhancedLeaderboardService.getParticipantComparison(sessionId, participantId);
    }

    @Operation(summary = "Get detailed leaderboard breakdown",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/breakdown")
    public Map<String, Object> getDetailedLeaderboardBreakdown(@PathVariable String sessionId) {
        return enhancedLeaderboardService.getDetailedLeaderboardBreakdown(sessionId);
    }

    @Operation(summary = "Broadcast leaderboard to host with context",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/session/{sessionId}/broadcast-host")
    public void broadcastHostLeaderboard(@PathVariable String sessionId) {
        enhancedLeaderboardService.broadcastHostLeaderboard(sessionId);
    }

    @Operation(summary = "Get animated leaderboard data for smooth transitions")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/animated")
    public Map<String, Object> getAnimatedLeaderboardData(@PathVariable String sessionId) {
        return enhancedLeaderboardService.getAnimatedLeaderboardData(sessionId);
    }

    @Operation(summary = "Get leaderboard trends and patterns",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/trends")
    public Map<String, Object> getLeaderboardTrends(@PathVariable String sessionId) {
        return enhancedLeaderboardService.getLeaderboardTrends(sessionId);
    }

    @Operation(summary = "Get enriched leaderboard with avatars and profiles")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/enriched")
    public LeaderboardResponse getEnrichedLeaderboard(@PathVariable String sessionId) {
        return enhancedLeaderboardService.getEnrichedLeaderboard(sessionId);
    }

    @Operation(summary = "Export leaderboard data in various formats",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/export")
    public Map<String, Object> exportLeaderboardData(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "json") String format) {
        return enhancedLeaderboardService.exportLeaderboardData(sessionId, format);
    }

    @Operation(summary = "Start streaming leaderboard updates",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/session/{sessionId}/stream")
    public void streamLeaderboardUpdates(@PathVariable String sessionId) {
        enhancedLeaderboardService.streamLeaderboardUpdates(sessionId);
    }

    @Operation(summary = "Update participant streaks",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/session/{sessionId}/participant/{participantId}/streak")
    public void updateParticipantStreaks(
            @PathVariable String sessionId,
            @PathVariable String participantId,
            @RequestParam boolean isCorrect) {
        enhancedLeaderboardService.updateParticipantStreaks(sessionId, participantId, isCorrect);
    }

    @Operation(summary = "Get leaderboard with streak information")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/with-streaks")
    public LeaderboardResponse getLeaderboardWithStreaks(@PathVariable String sessionId) {
        return enhancedLeaderboardService.getLeaderboardWithStreaks(sessionId);
    }
}
