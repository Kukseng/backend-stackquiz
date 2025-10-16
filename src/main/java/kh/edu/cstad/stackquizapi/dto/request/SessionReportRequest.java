package kh.edu.cstad.stackquizapi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionReportRequest {

    // Basic filters
    private String sessionCode;
    private List<String> participantIds; // Filter specific participants
    private List<Integer> questionNumbers; // Filter specific questions

    // Report customization
    private ReportType reportType;
    private List<ReportSection> includeSections;
    private ReportFormat format;
    private Boolean includeDetailedAnswers;
    private Boolean includePerformanceInsights;
    private Boolean includeRecommendations;

    // Data filters
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private Double minAccuracy;
    private Double maxAccuracy;
    private String completionStatus; // "ALL", "COMPLETED", "PARTIAL", "ABANDONED"

    // Sorting and pagination
    private String sortBy; // "score", "accuracy", "responseTime", "nickname"
    private String sortDirection; // "ASC", "DESC"
    private Integer page;
    private Integer size;

    public enum ReportType {
        SUMMARY,           // High-level overview
        DETAILED,          // Full participant and question analysis
        PARTICIPANT_FOCUS, // Focus on individual participant performance
        QUESTION_FOCUS,    // Focus on question analysis
        COMPARISON,        // Compare participants or sessions
        EXPORT            // For CSV/PDF export
    }

    public enum ReportSection {
        SESSION_OVERVIEW,
        SESSION_STATISTICS,
        QUESTION_ANALYSIS,
        PARTICIPANT_REPORTS,
        PERFORMANCE_INSIGHTS,
        ANSWER_BREAKDOWN,
        TIMING_ANALYSIS,
        RECOMMENDATIONS
    }

    public enum ReportFormat {
        JSON,
        CSV,
        PDF,
        EXCEL
    }
}