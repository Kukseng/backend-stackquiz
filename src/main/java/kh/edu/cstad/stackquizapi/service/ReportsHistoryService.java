package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.response.SessionSummaryResponse;

import java.util.List;

/**
 * Service interface for retrieving historical quiz session reports and summaries.
 * <p>
 * Allows hosts to access past session data, including all sessions or filtered
 * by specific status (e.g., completed, ongoing, cancelled).
 * </p>
 *
 * @author Phou Kukseng
 * @since 1.0
 */
public interface ReportsHistoryService {

    /**
     * Retrieve all session summaries for a specific host.
     *
     * @param hostId the unique ID of the host
     * @return a list of SessionSummaryResponse containing summaries of all sessions
     */
    List<SessionSummaryResponse> getHostSessionSummaries(String hostId);

    /**
     * Retrieve session summaries for a host filtered by status.
     *
     * @param hostId the unique ID of the host
     * @param status the session status to filter by (e.g., "COMPLETED", "ONGOING")
     * @return a list of SessionSummaryResponse matching the specified status
     */
    List<SessionSummaryResponse> getFilteredSessionSummaries(String hostId, String status);
}

