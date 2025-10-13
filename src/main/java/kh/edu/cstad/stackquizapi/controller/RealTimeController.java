package kh.edu.cstad.stackquizapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import kh.edu.cstad.stackquizapi.dto.websocket.HostProgressMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.LiveStatsMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.ParticipantRankingMessage;
import kh.edu.cstad.stackquizapi.service.RealTimeStatsService;
import kh.edu.cstad.stackquizapi.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/realtime")
public class RealTimeController {

    private final RealTimeStatsService realTimeStatsService;
    private final WebSocketService webSocketService;

    @Operation(summary = "Get live session statistics")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/stats")
    public LiveStatsMessage getLiveStats(@PathVariable String sessionId) {
        return realTimeStatsService.calculateLiveStats(sessionId);
    }

    @Operation(summary = "Get host progress information")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/host-progress")
    public HostProgressMessage getHostProgress(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "1") int currentQuestion) {
        return realTimeStatsService.calculateHostProgress(sessionId, currentQuestion);
    }

    @Operation(summary = "Get participant ranking information")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/participant/{participantId}/ranking")
    public ParticipantRankingMessage getParticipantRanking(
            @PathVariable String sessionId,
            @PathVariable String participantId) {
        return realTimeStatsService.calculateParticipantRanking(sessionId, participantId);
    }

    @Operation(summary = "Get current session rankings")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/rankings")
    public Map<String, Integer> getCurrentRankings(@PathVariable String sessionId) {
        return realTimeStatsService.getCurrentRankings(sessionId);
    }

    @Operation(summary = "Trigger live stats broadcast")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/session/{sessionId}/broadcast-stats")
    public void broadcastLiveStats(@PathVariable String sessionId) {
        LiveStatsMessage stats = realTimeStatsService.calculateLiveStats(sessionId);
        webSocketService.broadcastLiveStats(stats.getSessionId(), stats);
        log.info("Manually triggered live stats broadcast for session {}", sessionId);
    }

    @Operation(summary = "Trigger host progress broadcast")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/session/{sessionId}/broadcast-host-progress")
    public void broadcastHostProgress(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "1") int currentQuestion) {
        HostProgressMessage progress = realTimeStatsService.calculateHostProgress(sessionId, currentQuestion);
        webSocketService.broadcastHostProgress(progress.getSessionId(), progress);
        log.info("Manually triggered host progress broadcast for session {}", sessionId);
    }

    @Operation(summary = "Send ranking update to specific participant")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/session/{sessionId}/participant/{participantId}/send-ranking")
    public void sendParticipantRanking(
            @PathVariable String sessionId,
            @PathVariable String participantId) {
        ParticipantRankingMessage ranking = realTimeStatsService.calculateParticipantRanking(sessionId, participantId);
        webSocketService.sendRankingUpdateToParticipant(ranking.getSessionId(), participantId, ranking);
        log.info("Manually sent ranking update to participant {} in session {}", participantId, sessionId);
    }
}
