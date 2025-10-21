package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.Participant;
import kh.edu.cstad.stackquizapi.domain.Quiz;
import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.dto.response.AdminDashboardResponse;
import kh.edu.cstad.stackquizapi.repository.*;
import kh.edu.cstad.stackquizapi.service.AdminDashboardService;
import kh.edu.cstad.stackquizapi.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final QuizSessionRepository quizSessionRepository;
    private final ParticipantRepository participantRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final ParticipantAnswerRepository participantAnswerRepository;
    private final UserRepository userRepository;

    @Override
    public AdminDashboardResponse getAdminDashboardStats() {
        log.info("Fetching admin dashboard statistics");

        // Session Statistics
        Long totalSessions = quizSessionRepository.count();
        Long activeSessions = quizSessionRepository.countByStatus(Status.IN_PROGRESS);
        Long completedSessions = quizSessionRepository.countByStatus(Status.ENDED);
        Long scheduledSessions = quizSessionRepository.countByStatus(Status.WAITING);

        // Participant Statistics
        Long totalParticipants = participantRepository.count();
        Long activeParticipants = participantRepository.countByIsActiveTrue();
        Long totalUniqueParticipants = participantRepository.countDistinctNickname();

        // Quiz Statistics
        Long totalQuizzes = quizRepository.count();
        Long totalQuestions = questionRepository.count();
        Long totalAnswers = participantAnswerRepository.count();

        // Engagement Metrics
        Double averageParticipantsPerSession = totalSessions > 0 
            ? (double) totalParticipants / totalSessions 
            : 0.0;

        Double averageSessionDuration = calculateAverageSessionDuration();
        Double overallAccuracyRate = calculateOverallAccuracyRate();

        // Time-based Statistics
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusDays(7);
        LocalDateTime startOfMonth = now.minusDays(30);

        Long sessionsToday = quizSessionRepository.countByCreatedAtAfter(startOfToday);
        Long sessionsThisWeek = quizSessionRepository.countByCreatedAtAfter(startOfWeek);
        Long sessionsThisMonth = quizSessionRepository.countByCreatedAtAfter(startOfMonth);

        Long participantsToday = participantRepository.countByJoinedAtAfter(startOfToday);
        Long participantsThisWeek = participantRepository.countByJoinedAtAfter(startOfWeek);
        Long participantsThisMonth = participantRepository.countByJoinedAtAfter(startOfMonth);

        // Popular Quizzes
        List<AdminDashboardResponse.PopularQuizStats> topQuizzes = getTopQuizzes();

        // Recent Activity
        QuizSession lastCreatedSession = quizSessionRepository.findTopByOrderByCreatedAtDesc();
        QuizSession lastCompletedSession = quizSessionRepository.findTopByStatusOrderByEndTimeDesc(Status.ENDED);
        
        LocalDateTime lastSessionCreated = lastCreatedSession != null ? lastCreatedSession.getCreatedAt() : null;
        LocalDateTime lastSessionCompleted = lastCompletedSession != null ? lastCompletedSession.getEndTime() : null;
        
        String mostActiveHost = getMostActiveHost();

        return AdminDashboardResponse.builder()
                .totalSessions(totalSessions)
                .activeSessions(activeSessions)
                .completedSessions(completedSessions)
                .scheduledSessions(scheduledSessions)
                .totalParticipants(totalParticipants)
                .activeParticipants(activeParticipants)
                .totalUniqueParticipants(totalUniqueParticipants)
                .totalQuizzes(totalQuizzes)
                .totalQuestions(totalQuestions)
                .totalAnswers(totalAnswers)
                .averageParticipantsPerSession(averageParticipantsPerSession)
                .averageSessionDuration(averageSessionDuration)
                .overallAccuracyRate(overallAccuracyRate)
                .sessionsToday(sessionsToday)
                .sessionsThisWeek(sessionsThisWeek)
                .sessionsThisMonth(sessionsThisMonth)
                .participantsToday(participantsToday)
                .participantsThisWeek(participantsThisWeek)
                .participantsThisMonth(participantsThisMonth)
                .topQuizzes(topQuizzes)
                .lastSessionCreated(lastSessionCreated)
                .lastSessionCompleted(lastSessionCompleted)
                .mostActiveHost(mostActiveHost)
                .build();
    }

    @Override
    public AdminDashboardResponse getAdminDashboardStatsByPeriod(int days) {
        log.info("Fetching admin dashboard statistics for last {} days", days);
        
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        
        // Similar to getAdminDashboardStats but filtered by date
        Long totalSessions = quizSessionRepository.countByCreatedAtAfter(startDate);
        Long activeSessions = quizSessionRepository.countByStatusAndCreatedAtAfter(Status.IN_PROGRESS, startDate);
        Long completedSessions = quizSessionRepository.countByStatusAndCreatedAtAfter(Status.ENDED, startDate);
        
        Long totalParticipants = participantRepository.countByJoinedAtAfter(startDate);
        Long totalAnswers = participantAnswerRepository.countByAnsweredAtAfter(startDate);
        
        Double averageParticipantsPerSession = totalSessions > 0 
            ? (double) totalParticipants / totalSessions 
            : 0.0;
        
        return AdminDashboardResponse.builder()
                .totalSessions(totalSessions)
                .activeSessions(activeSessions)
                .completedSessions(completedSessions)
                .totalParticipants(totalParticipants)
                .totalAnswers(totalAnswers)
                .averageParticipantsPerSession(averageParticipantsPerSession)
                .build();
    }

    private Double calculateAverageSessionDuration() {
        try {
            List<QuizSession> completedSessions = quizSessionRepository.findByStatus(Status.ENDED);
            
            if (completedSessions.isEmpty()) {
                return 0.0;
            }
            
            double totalMinutes = completedSessions.stream()
                    .filter(session -> session.getStartTime() != null && session.getEndTime() != null)
                    .mapToDouble(session -> {
                        Duration duration = Duration.between(session.getStartTime(), session.getEndTime());
                        return duration.toMinutes();
                    })
                    .average()
                    .orElse(0.0);
            
            return totalMinutes;
        } catch (Exception e) {
            log.error("Error calculating average session duration: {}", e.getMessage());
            return 0.0;
        }
    }

    private Double calculateOverallAccuracyRate() {
        try {
            Long totalAnswers = participantAnswerRepository.count();
            if (totalAnswers == 0) {
                return 0.0;
            }
            
            Long correctAnswers = participantAnswerRepository.countByIsCorrectTrue();
            return (correctAnswers.doubleValue() / totalAnswers.doubleValue()) * 100.0;
        } catch (Exception e) {
            log.error("Error calculating overall accuracy rate: {}", e.getMessage());
            return 0.0;
        }
    }

    private List<AdminDashboardResponse.PopularQuizStats> getTopQuizzes() {
        try {
            // Get all sessions grouped by quiz
            List<QuizSession> allSessions = quizSessionRepository.findAll();
            
            Map<Quiz, List<QuizSession>> sessionsByQuiz = allSessions.stream()
                    .filter(session -> session.getQuiz() != null)
                    .collect(Collectors.groupingBy(QuizSession::getQuiz));
            
            return sessionsByQuiz.entrySet().stream()
                    .map(entry -> {
                        Quiz quiz = entry.getKey();
                        List<QuizSession> sessions = entry.getValue();
                        
                        Long timesPlayed = (long) sessions.size();
                        Long totalParticipants = sessions.stream()
                                .mapToLong(session -> participantRepository.countBySessionId(session.getId()))
                                .sum();
                        
                        Double averageAccuracy = calculateQuizAccuracy(quiz.getId());
                        
                        return AdminDashboardResponse.PopularQuizStats.builder()
                                .quizId(quiz.getId())
                                .quizTitle(quiz.getTitle())
                                .timesPlayed(timesPlayed)
                                .totalParticipants(totalParticipants)
                                .averageAccuracy(averageAccuracy)
                                .build();
                    })
                    .sorted((a, b) -> Long.compare(b.getTimesPlayed(), a.getTimesPlayed()))
                    .limit(5)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting top quizzes: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private Double calculateQuizAccuracy(String quizId) {
        try {
            List<QuizSession> sessions = quizSessionRepository.findByQuizId(quizId);
            List<String> sessionIds = sessions.stream()
                    .map(QuizSession::getId)
                    .collect(Collectors.toList());
            
            if (sessionIds.isEmpty()) {
                return 0.0;
            }
            
            List<Participant> participants = participantRepository.findBySessionIdIn(sessionIds);
            List<String> participantIds = participants.stream()
                    .map(Participant::getId)
                    .collect(Collectors.toList());
            
            if (participantIds.isEmpty()) {
                return 0.0;
            }
            
            Long totalAnswers = participantAnswerRepository.countByParticipantIdIn(participantIds);
            if (totalAnswers == 0) {
                return 0.0;
            }
            
            Long correctAnswers = participantAnswerRepository.countByParticipantIdInAndIsCorrectTrue(participantIds);
            return (correctAnswers.doubleValue() / totalAnswers.doubleValue()) * 100.0;
        } catch (Exception e) {
            log.error("Error calculating quiz accuracy: {}", e.getMessage());
            return 0.0;
        }
    }

    private String getMostActiveHost() {
        try {
            List<QuizSession> allSessions = quizSessionRepository.findAll();
            
            Map<String, Long> sessionsByHost = allSessions.stream()
                    .filter(session -> session.getHost() != null)
                    .collect(Collectors.groupingBy(
                            session -> session.getHost().getUsername(),
                            Collectors.counting()
                    ));
            
            return sessionsByHost.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");
        } catch (Exception e) {
            log.error("Error getting most active host: {}", e.getMessage());
            return "N/A";
        }
    }
}

