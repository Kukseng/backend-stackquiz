package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SessionTimingRequest(
        LocalDateTime scheduledStartTime,
        LocalDateTime scheduledEndTime,
        
        @Min(value = 5, message = "Default question time limit must be at least 5 seconds")
        Integer defaultQuestionTimeLimit,
        
        @Min(value = 1, message = "Current question time limit must be at least 1 second")
        Integer currentQuestionTimeLimit,
        
        Boolean autoAdvanceQuestions,
        Boolean allowLateJoining,
        Boolean showTimer,
        Boolean enableBreaks,
        
        @Min(value = 0, message = "Break duration cannot be negative")
        Integer breakDuration,
        
        String timeZone
) {
    public SessionTimingRequest {
        // Validation: end time must be after start time
        if (scheduledStartTime != null && scheduledEndTime != null) {
            if (scheduledEndTime.isBefore(scheduledStartTime)) {
                throw new IllegalArgumentException("Scheduled end time must be after start time");
            }
        }
        
        // Default values
        if (defaultQuestionTimeLimit == null) {
            defaultQuestionTimeLimit = 30;
        }
        if (autoAdvanceQuestions == null) {
            autoAdvanceQuestions = false;
        }
        if (allowLateJoining == null) {
            allowLateJoining = true;
        }
        if (showTimer == null) {
            showTimer = true;
        }
        if (enableBreaks == null) {
            enableBreaks = false;
        }
        if (breakDuration == null) {
            breakDuration = 60;
        }
        if (timeZone == null) {
            timeZone = "UTC";
        }
    }
}
