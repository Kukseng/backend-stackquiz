package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.Feedback;
import kh.edu.cstad.stackquizapi.domain.Quiz;
import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.dto.request.FeedbackRequest;
import kh.edu.cstad.stackquizapi.dto.response.FeedbackResponse;
import kh.edu.cstad.stackquizapi.mapper.FeedbackMapper;
import kh.edu.cstad.stackquizapi.repository.FeedbackRepository;
import kh.edu.cstad.stackquizapi.repository.QuizRepository;
import kh.edu.cstad.stackquizapi.repository.QuizSessionRepository;
import kh.edu.cstad.stackquizapi.service.FeedbackService;
import kh.edu.cstad.stackquizapi.util.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final QuizRepository quizRepository;
    private final QuizSessionRepository quizSessionRepository;
    private final FeedbackMapper feedbackMapper;

    @Override
    public FeedbackResponse submitFeedback(String quizId, String sessionId, FeedbackRequest feedbackRequest) {

        //search Quiz
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Quiz Not Found"));

        //search Session
        QuizSession quizSession = quizSessionRepository.findById(sessionId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Session Not Found"));

        //allow feedback only session Status is ENDED
        if(quizSession.getStatus() != Status.ENDED){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Session Not Ended yet");
        }

        // create feed back
        Feedback feedback = new Feedback();
        feedback.setQuiz(quiz);
        feedback.setSession(quizSession);
        feedback.setComment(feedbackRequest.comment());
        feedback.setRating(feedbackRequest.rating());
        feedback.setCreatedAt(LocalDateTime.now());

        // save to database
        feedback = feedbackRepository.save(feedback);

        // return Response
        return feedbackMapper.toDto(feedback);

    }

    @Override
    public List<FeedbackResponse> getFeedbackBySessionId(String sessionId) {

        //search Session
        QuizSession quizSession = quizSessionRepository.findById(sessionId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Session Not Found"));

        // Get all feedback for this session and convert to DTOs
        return feedbackRepository.findBySession(quizSession)
                .stream()
                .map(feedbackMapper::toDto)
                .toList();
    }
}
