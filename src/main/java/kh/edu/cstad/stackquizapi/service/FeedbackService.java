package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.FeedbackRequest;
import kh.edu.cstad.stackquizapi.dto.response.FeedbackResponse;

import java.util.List;

public interface FeedbackService {

    FeedbackResponse submitFeedback(String quizId,String sessionId,FeedbackRequest feedbackRequest);

    List<FeedbackResponse> getFeedbackBySessionId(String sessionId);

}
