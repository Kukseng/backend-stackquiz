package kh.edu.cstad.stackquizapi.controller;

import kh.edu.cstad.stackquizapi.dto.request.FeedbackSummaryRequest;
import kh.edu.cstad.stackquizapi.dto.response.FeedbackSummaryResponse;
import kh.edu.cstad.stackquizapi.service.FeedbackSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/feedback-summary")
public class FeedbackSummaryController {

    private final FeedbackSummaryService feedbackSummaryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeedbackSummaryResponse createFeedbackSummary(
            @RequestBody FeedbackSummaryRequest feedbackSummaryRequest
    ){
        return feedbackSummaryService.createFeedbackSummary(feedbackSummaryRequest);
    }

    @GetMapping("/sessionId/{sessionId}")
    @ResponseStatus(HttpStatus.OK)
    public FeedbackSummaryResponse getFeedbackSummaryBySessionId(
            @PathVariable String sessionId
    ){
        return feedbackSummaryService.getFeedbackSummaryBySessionId(sessionId);
    }

}
