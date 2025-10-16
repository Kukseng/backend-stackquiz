package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.Participant;
import kh.edu.cstad.stackquizapi.domain.ParticipantAnswer;
import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.dto.response.SessionSummaryResponse;
import kh.edu.cstad.stackquizapi.repository.ParticipantAnswerRepository;
import kh.edu.cstad.stackquizapi.repository.ParticipantRepository;
import kh.edu.cstad.stackquizapi.repository.QuizSessionRepository;
import kh.edu.cstad.stackquizapi.service.ReportsHistoryService;
import kh.edu.cstad.stackquizapi.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportsHistoryServiceImpl implements ReportsHistoryService {

    private final QuizSessionRepository quizSessionRepository;
    private final ParticipantRepository participantRepository;
    private final ParticipantAnswerRepository participantAnswerRepository;

    @Override
    public List<SessionSummaryResponse> getHostSessionSummaries(String hostId) {
        log.info("Fetching all session summaries for host: {}", hostId);
        
        List<QuizSession> sessions = quizSessionRepository.findByHostIdOrderByCreatedAtDesc(hostId);
        
        return sessions.stream()
                .map(this::buildSessionSummary)
                .collect(Collectors.toList());
    }

    @Override
    public List<SessionSummaryResponse> getFilteredSessionSummaries(String hostId, String statusFilter) {
        log.info("Fetching filtered session summaries for host: {} with status: {}", hostId, statusFilter);
        
        List<QuizSession> sessions = quizSessionRepository.findByHostIdOrderByCreatedAtDesc(hostId);
        
        // Filter by status if provided
        if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equalsIgnoreCase("ALL")) {
            Status status = Status.valueOf(statusFilter.toUpperCase());
            sessions = sessions.stream()
                    .filter(session -> session.getStatus() == status)
                    .collect(Collectors.toList());
        }
        
        return sessions.stream()
                .map(this::buildSessionSummary)
                .collect(Collectors.toList());
    }

    private SessionSummaryResponse buildSessionSummary(QuizSession session) {
        // Get participants for this session
        List<Participant> participants = participantRepository.findBySessionId(session.getId());
        int totalParticipants = participants.size();
        
        // Calculate statistics
        double averageAccuracy = 0.0;
        double completionRate = 0.0;
        
        if (totalParticipants > 0) {
            // Get all answers for this session
            List<ParticipantAnswer> allAnswers = participantAnswerRepository.findByParticipantSessionId(session.getId());
            
            // Calculate average accuracy
            if (!allAnswers.isEmpty()) {
                long correctAnswers = allAnswers.stream()
                        .filter(ParticipantAnswer::getIsCorrect)
                        .count();
                averageAccuracy = (double) correctAnswers / allAnswers.size() * 100;
            }
            
            // Calculate completion rate
            int totalQuestions = session.getTotalQuestions();
            if (totalQuestions > 0) {
                long completedParticipants = participants.stream()
                        .filter(p -> isParticipantCompleted(p, totalQuestions))
                        .count();
                completionRate = (double) completedParticipants / totalParticipants * 100;
            }
        }
        
        return SessionSummaryResponse.builder()
                .sessionId(session.getId())
                .sessionCode(session.getSessionCode())
                .sessionName(session.getSessionName() != null ? session.getSessionName() : "Untitled Session")
                .quizTitle(session.getQuiz() != null ? session.getQuiz().getTitle() : "Unknown Quiz")
                .status(session.getStatus().toString())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .totalParticipants(totalParticipants)
                .averageAccuracy(Math.round(averageAccuracy * 100.0) / 100.0)
                .completionRate(Math.round(completionRate * 100.0) / 100.0)
                .hostName(session.getHostName())
                .totalQuestions(session.getTotalQuestions())
                .build();
    }
    
    private boolean isParticipantCompleted(Participant participant, int totalQuestions) {
        // Get participant's answers
        List<ParticipantAnswer> answers = participantAnswerRepository.findByParticipantIdOrderByAnsweredAt(participant.getId());
        
        // Consider completed if answered at least 80% of questions
        return answers.size() >= (totalQuestions * 0.8);
    }
}

