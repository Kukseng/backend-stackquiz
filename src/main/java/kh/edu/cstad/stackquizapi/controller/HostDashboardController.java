package kh.edu.cstad.stackquizapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kh.edu.cstad.stackquizapi.dto.request.SessionTimingRequest;
import kh.edu.cstad.stackquizapi.dto.response.HostDashboardResponse;
import kh.edu.cstad.stackquizapi.dto.websocket.HostProgressMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.SessionTimerMessage;
import kh.edu.cstad.stackquizapi.service.HostDashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/host")
public class HostDashboardController {

    private final HostDashboardService hostDashboardService;

    @Operation(summary = "Get comprehensive host dashboard",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/dashboard/{sessionCode}")
    public HostDashboardResponse getHostDashboard(@PathVariable String sessionCode) {
        return hostDashboardService.getHostDashboard(sessionCode);
    }

    @Operation(summary = "Update session timing settings",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/session/{sessionId}/timing")
    public void updateSessionTiming(@PathVariable String sessionId,
                                    @Valid @RequestBody SessionTimingRequest request) {
        hostDashboardService.updateSessionTiming(sessionId, request);
    }

    @Operation(summary = "Start session timer",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/session/{sessionId}/timer/start")
    public void startSessionTimer(@PathVariable String sessionId) {
        hostDashboardService.startSessionTimer(sessionId);
    }

    @Operation(summary = "Pause session timer",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/session/{sessionId}/timer/pause")
    public void pauseSessionTimer(@PathVariable String sessionId) {
        hostDashboardService.pauseSessionTimer(sessionId);
    }

    @Operation(summary = "Resume session timer",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/session/{sessionId}/timer/resume")
    public void resumeSessionTimer(@PathVariable String sessionId) {
        hostDashboardService.resumeSessionTimer(sessionId);
    }

    @Operation(summary = "End session timer",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/session/{sessionId}/timer/end")
    public void endSessionTimer(@PathVariable String sessionId) {
        hostDashboardService.endSessionTimer(sessionId);
    }

    @Operation(summary = "Get current session timer status",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/timer")
    public SessionTimerMessage getSessionTimer(@PathVariable String sessionId) {
        return hostDashboardService.getSessionTimer(sessionId);
    }

    @Operation(summary = "Set dynamic question time limit",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/session/{sessionId}/question-time-limit")
    public void setQuestionTimeLimit(@PathVariable String sessionId,
                                     @RequestParam int timeLimit) {
        hostDashboardService.setQuestionTimeLimit(sessionId, timeLimit);
    }

    @Operation(summary = "Get real-time participant progress",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/participant-progress")
    public List<HostProgressMessage.ParticipantProgress> getParticipantProgress(@PathVariable String sessionId) {
        return hostDashboardService.getParticipantProgress(sessionId);
    }

    @Operation(summary = "Get current question statistics",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/question/{questionId}/stats")
    public Map<String, Object> getCurrentQuestionStats(@PathVariable String sessionId,
                                                       @PathVariable String questionId) {
        return hostDashboardService.getCurrentQuestionStats(sessionId, questionId);
    }

    @Operation(summary = "Force advance to next question",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/session/{sessionId}/force-advance")
    public void forceAdvanceQuestion(@PathVariable String sessionId) {
        hostDashboardService.forceAdvanceQuestion(sessionId);
    }

    @Operation(summary = "Get session performance analytics",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/analytics")
    public Map<String, Object> getSessionAnalytics(@PathVariable String sessionId) {
        return hostDashboardService.getSessionAnalytics(sessionId);
    }

    @Operation(summary = "Schedule automatic session start",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/session/{sessionId}/schedule-start")
    public void scheduleSessionStart(@PathVariable String sessionId,
                                     @RequestParam LocalDateTime startTime) {
        hostDashboardService.scheduleSessionStart(sessionId, startTime);
    }

    @Operation(summary = "Schedule automatic session end",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/session/{sessionId}/schedule-end")
    public void scheduleSessionEnd(@PathVariable String sessionId,
                                   @RequestParam LocalDateTime endTime) {
        hostDashboardService.scheduleSessionEnd(sessionId, endTime);
    }

    @Operation(summary = "Cancel scheduled session events",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/session/{sessionId}/scheduled-events")
    public void cancelScheduledEvents(@PathVariable String sessionId) {
        hostDashboardService.cancelScheduledEvents(sessionId);
    }

    @Operation(summary = "Get session timing information",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/timing")
    public Map<String, Object> getSessionTiming(@PathVariable String sessionId) {
        return hostDashboardService.getSessionTiming(sessionId);
    }

    @Operation(summary = "Broadcast host progress updates",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/session/{sessionId}/broadcast-progress")
    public void broadcastHostProgress(@PathVariable String sessionId) {
        hostDashboardService.broadcastHostProgress(sessionId);
    }

    @Operation(summary = "Send notification to host",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/session/{sessionId}/notification")
    public void sendHostNotification(@PathVariable String sessionId,
                                     @RequestParam String message,
                                     @RequestParam(defaultValue = "INFO") String type) {
        hostDashboardService.sendHostNotification(sessionId, message, type);
    }

    @Operation(summary = "Get answer distribution for current question",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/question/{questionId}/answer-distribution")
    public Map<String, Integer> getAnswerDistribution(@PathVariable String sessionId,
                                                      @PathVariable String questionId) {
        return hostDashboardService.getAnswerDistribution(sessionId, questionId);
    }

    @Operation(summary = "Export session data for analysis",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/session/{sessionId}/export")
    public Map<String, Object> exportSessionData(@PathVariable String sessionId) {
        return hostDashboardService.exportSessionData(sessionId);
    }

    @Operation(summary = "Broadcast timer update to all participants",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/session/{sessionId}/broadcast-timer")
    public void broadcastTimerUpdate(@PathVariable String sessionId) {
        hostDashboardService.broadcastTimerUpdate(sessionId);
    }
}
