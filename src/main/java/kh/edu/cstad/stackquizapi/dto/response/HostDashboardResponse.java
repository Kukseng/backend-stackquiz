package kh.edu.cstad.stackquizapi.dto.response;

import kh.edu.cstad.stackquizapi.dto.websocket.HostProgressMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.LiveStatsMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.SessionTimerMessage;
import kh.edu.cstad.stackquizapi.util.Status;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
public record HostDashboardResponse(

        // Session basic info
        String sessionId,
        String sessionCode,
        String sessionName,
        Status sessionStatus,
        LocalDateTime createdAt,
        LocalDateTime startTime,
        LocalDateTime endTime,
        
        // Timing information
        LocalDateTime scheduledStartTime,
        LocalDateTime scheduledEndTime,
        Integer defaultQuestionTimeLimit,
        Boolean autoAdvanceQuestions,
        Boolean allowLateJoining,
        SessionTimerMessage currentTimer,
        
        // Progress information
        Integer currentQuestion,
        Integer totalQuestions,
        Integer totalParticipants,
        Integer activeParticipants,
        Integer participantsAnswered,
        Integer participantsPending,
        
        // Real-time statistics
        LiveStatsMessage liveStats,
        HostProgressMessage hostProgress,
        List<HostProgressMessage.ParticipantProgress> participantProgress,
        
        // Current question data
        String currentQuestionId,
        String currentQuestionText,
        Map<String, Integer> answerDistribution,
        Double currentQuestionAccuracy,
        Double averageResponseTime,
        
        // Leaderboard information
        LeaderboardResponse currentLeaderboard,
        List<ParticipantResponse> topPerformers,
        
        // Session analytics
        Map<String, Object> sessionAnalytics,
        Map<String, Object> performanceMetrics,
        
        // Control states
        Boolean canStart,
        Boolean canPause,
        Boolean canResume,
        Boolean canEnd,
        Boolean canAdvanceQuestion,
        Boolean canGoBack,
        
        // Notifications and alerts
        List<String> activeAlerts,
        List<String> recentNotifications,
        
        // Export and reporting
        Boolean canExportData,
        String reportUrl,
        
        Long lastUpdated
) {
    public HostDashboardResponse {
        if (lastUpdated == null) {
            lastUpdated = System.currentTimeMillis();
        }
    }
}
