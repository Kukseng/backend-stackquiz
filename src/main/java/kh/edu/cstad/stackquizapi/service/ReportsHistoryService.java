package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.response.SessionSummaryResponse;

import java.util.List;

public interface ReportsHistoryService {
    
    /**
     * Get all session summaries for a specific host
     */
    List<SessionSummaryResponse> getHostSessionSummaries(String hostId);
    
    /**
     * Get filtered session summaries by status
     */
    List<SessionSummaryResponse> getFilteredSessionSummaries(String hostId, String status);
}

