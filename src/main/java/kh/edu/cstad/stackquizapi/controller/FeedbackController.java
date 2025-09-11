package kh.edu.cstad.stackquizapi.controller;

import kh.edu.cstad.stackquizapi.domain.Feedback;
import kh.edu.cstad.stackquizapi.dto.request.FeedbackRequest;
import kh.edu.cstad.stackquizapi.dto.response.FeedbackResponse;
import kh.edu.cstad.stackquizapi.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    // CREATE FEEDBACK FOR A SINGLE FEEDBACK
    @PostMapping("/quizId/{quizId}/sessionId/{sessionId}")
    @ResponseStatus(HttpStatus.CREATED)
    public FeedbackResponse submitFeedback(
            @PathVariable String quizId,
            @PathVariable String sessionId,
            @RequestBody FeedbackRequest feedbackRequest) {
        return feedbackService.submitFeedback(quizId, sessionId, feedbackRequest);
    }

//    Get all feedback for a session
    @GetMapping("/session/{sessionId}")
    public List<FeedbackResponse> getFeedbackBySessionId(@PathVariable String sessionId) {
        return feedbackService.getFeedbackBySessionId(sessionId);
    }

}
