package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.FeedbackSummaryRequest;
import kh.edu.cstad.stackquizapi.dto.response.FeedbackSummaryResponse;

public interface FeedbackSummaryService {
    FeedbackSummaryResponse createFeedbackSummary(FeedbackSummaryRequest feedbackSummaryRequest);

    FeedbackSummaryResponse getFeedbackSummaryBySessionId(String sessionId);
}
