package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.FeedbackSummary;
import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.dto.request.FeedbackSummaryRequest;
import kh.edu.cstad.stackquizapi.dto.response.FeedbackSummaryResponse;
import kh.edu.cstad.stackquizapi.repository.FeedbackRepository;
import kh.edu.cstad.stackquizapi.repository.FeedbackSummaryRepository;
import kh.edu.cstad.stackquizapi.repository.QuizSessionRepository;
import kh.edu.cstad.stackquizapi.service.FeedbackSummaryService;
import kh.edu.cstad.stackquizapi.util.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FeedbackSummaryServiceImpl  implements FeedbackSummaryService {

    private final FeedbackSummaryRepository feedbackSummaryRepository;
    private final FeedbackRepository feedbackRepository;
    private final QuizSessionRepository quizSessionRepository;


    @Override
    public FeedbackSummaryResponse createFeedbackSummary(FeedbackSummaryRequest feedbackSummaryRequest) {

        //1. Find Quiz Session
        QuizSession quizSession = quizSessionRepository.findById(feedbackSummaryRequest.sessionId())
                .orElseThrow(() -> new RuntimeException("Quiz session not found"));

        //2. Check If Session Is Ended
        if(quizSession.getStatus() != Status.ENDED){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session not ended yet");
        }

        //3. Check Feedback Summary Already Exists For this Session
        if(feedbackSummaryRepository.findBySession_Id(feedbackSummaryRequest.sessionId()).isPresent()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session already exists");
        }

        //4. Count Feedback for this session
        int totalFeedback = feedbackRepository.findBySession(quizSession).size();

        //5. Save New Feedback Summary
        FeedbackSummary feedbackSummary = new FeedbackSummary();
        feedbackSummary.setSession(quizSession);
        feedbackSummary.setTotalFeedback(totalFeedback);
        feedbackSummary.setCreatedAt(LocalDateTime.now());
        feedbackSummaryRepository.save(feedbackSummary);

        return new FeedbackSummaryResponse(quizSession.getId(), totalFeedback);
    }

    @Override
    public FeedbackSummaryResponse getFeedbackSummaryBySessionId(String sessionId) {
        //1. Find FeedbackSummary
        FeedbackSummary feedbackSummary = feedbackSummaryRepository.findBySession_Id(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        return new FeedbackSummaryResponse(feedbackSummary.getSession().getId(), feedbackSummary.getTotalFeedback());
    }
}
