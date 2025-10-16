package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.Participant;
import kh.edu.cstad.stackquizapi.domain.Quiz;
import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.dto.response.UserActivityResponse;
import kh.edu.cstad.stackquizapi.repository.ParticipantRepository;
import kh.edu.cstad.stackquizapi.repository.QuizRepository;
import kh.edu.cstad.stackquizapi.repository.QuizSessionRepository;
import kh.edu.cstad.stackquizapi.service.UserActivityService;
import kh.edu.cstad.stackquizapi.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActivityServiceImpl implements UserActivityService {

    private final QuizRepository quizRepository;
    private final QuizSessionRepository quizSessionRepository;
    private final ParticipantRepository participantRepository;

    @Override
    public UserActivityResponse getUserActivity(String userId) {
        return getUserActivityByTimeRange(userId, "all");
    }

    @Override
    public UserActivityResponse getUserActivityByTimeRange(String userId, String timeRange) {
        log.info("Calculating activity statistics for user: {} with time range: {}", userId, timeRange);

        LocalDateTime startDate = getStartDateForTimeRange(timeRange);
        
        // Fetch all user data
        List<Quiz> allQuizzes = quizRepository.findByUser_Id(userId);
        List<Quiz> quizzes = filterByTimeRange(allQuizzes, startDate);
        
        List<QuizSession> allSessions = quizSessionRepository.findByHostIdOrderByCreatedAtDesc(userId);
        List<QuizSession> sessions = filterSessionsByTimeRange(allSessions, startDate);

        // Calculate basic statistics
        Integer totalQuizzesCreated = quizzes.size();
        Integer totalSessionsStarted = sessions.size();
        Integer totalQuestionsCreated = quizzes.stream()
                .mapToInt(q -> q.getQuestions() != null ? q.getQuestions().size() : 0)
                .sum();

        // Calculate participant statistics
        Integer totalParticipants = 0;
        Integer totalAnswersReceived = 0;
        for (QuizSession session : sessions) {
            List<Participant> participants = participantRepository.findBySessionId(session.getId());
            totalParticipants += participants.size();
            totalAnswersReceived += participants.stream()
                    .mapToInt(p -> p.getAnswers() != null ? p.getAnswers().size() : 0)
                    .sum();
        }

        // Session status counts
        Integer activeSessionsCount = (int) sessions.stream()
                .filter(s -> s.getStatus() == Status.IN_PROGRESS || s.getStatus() == Status.WAITING)
                .count();
        
        Integer completedSessionsCount = (int) sessions.stream()
                .filter(s -> s.getStatus() == Status.COMPLETED || s.getStatus() == Status.ENDED)
                .count();

        // Calculate engagement metrics
        Double averageParticipantsPerSession = sessions.isEmpty() ? 0.0 : 
                (double) totalParticipants / sessions.size();
        
        Double averageQuestionsPerQuiz = quizzes.isEmpty() ? 0.0 : 
                (double) totalQuestionsCreated / quizzes.size();
        
        Double sessionCompletionRate = allSessions.isEmpty() ? 0.0 : 
                (double) completedSessionsCount / allSessions.size() * 100;

        // Time-based statistics
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        Integer quizzesCreatedThisWeek = (int) allQuizzes.stream()
                .filter(q -> q.getCreatedAt() != null && q.getCreatedAt().isAfter(oneWeekAgo))
                .count();
        
        Integer quizzesCreatedThisMonth = (int) allQuizzes.stream()
                .filter(q -> q.getCreatedAt() != null && q.getCreatedAt().isAfter(oneMonthAgo))
                .count();

        Integer sessionsStartedThisWeek = (int) allSessions.stream()
                .filter(s -> s.getCreatedAt() != null && s.getCreatedAt().isAfter(oneWeekAgo))
                .count();
        
        Integer sessionsStartedThisMonth = (int) allSessions.stream()
                .filter(s -> s.getCreatedAt() != null && s.getCreatedAt().isAfter(oneMonthAgo))
                .count();

        // Most popular quiz
        UserActivityResponse.MostPopularQuiz mostPopularQuiz = getMostPopularQuiz(allQuizzes, allSessions);

        // Recent activities
        List<UserActivityResponse.RecentActivity> recentActivities = getRecentActivities(allQuizzes, allSessions);

        // Time series data for graphs
        List<UserActivityResponse.TimeSeriesData> quizCreationTimeSeries = 
                generateQuizCreationTimeSeries(quizzes, timeRange);
        
        List<UserActivityResponse.TimeSeriesData> sessionActivityTimeSeries = 
                generateSessionActivityTimeSeries(sessions, timeRange);
        
        List<UserActivityResponse.TimeSeriesData> participantGrowthTimeSeries = 
                generateParticipantGrowthTimeSeries(sessions, timeRange);

        // Category and difficulty breakdown
        Map<String, Integer> quizzesByCategory = getQuizzesByCategory(quizzes);
        Map<String, Integer> quizzesByDifficulty = getQuizzesByDifficulty(quizzes);

        // Session statistics
        Map<String, Integer> sessionsByMode = getSessionsByMode(sessions);
        Map<String, Integer> sessionsByStatus = getSessionsByStatus(sessions);

        // Peak activity times
        Map<String, Integer> activityByDayOfWeek = getActivityByDayOfWeek(allQuizzes, allSessions);
        Map<String, Integer> activityByHourOfDay = getActivityByHourOfDay(allQuizzes, allSessions);

        // Timestamps
        LocalDateTime firstQuizCreatedAt = allQuizzes.stream()
                .map(Quiz::getCreatedAt)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        
        LocalDateTime lastActivityAt = getLastActivityTime(allQuizzes, allSessions);
        
        Long memberSince = firstQuizCreatedAt != null ? 
                ChronoUnit.DAYS.between(firstQuizCreatedAt, LocalDateTime.now()) : 0L;

        return UserActivityResponse.builder()
                .userId(userId)
                .username(userId) // You might want to fetch actual username
                .totalQuizzesCreated(totalQuizzesCreated)
                .totalSessionsStarted(totalSessionsStarted)
                .totalParticipants(totalParticipants)
                .totalQuestionsCreated(totalQuestionsCreated)
                .activeSessionsCount(activeSessionsCount)
                .completedSessionsCount(completedSessionsCount)
                .averageParticipantsPerSession(Math.round(averageParticipantsPerSession * 100.0) / 100.0)
                .averageQuestionsPerQuiz(Math.round(averageQuestionsPerQuiz * 100.0) / 100.0)
                .sessionCompletionRate(Math.round(sessionCompletionRate * 100.0) / 100.0)
                .totalAnswersReceived(totalAnswersReceived)
                .quizzesCreatedThisWeek(quizzesCreatedThisWeek)
                .quizzesCreatedThisMonth(quizzesCreatedThisMonth)
                .sessionsStartedThisWeek(sessionsStartedThisWeek)
                .sessionsStartedThisMonth(sessionsStartedThisMonth)
                .mostPopularQuiz(mostPopularQuiz)
                .recentActivities(recentActivities)
                .quizCreationTimeSeries(quizCreationTimeSeries)
                .sessionActivityTimeSeries(sessionActivityTimeSeries)
                .participantGrowthTimeSeries(participantGrowthTimeSeries)
                .quizzesByCategory(quizzesByCategory)
                .quizzesByDifficulty(quizzesByDifficulty)
                .sessionsByMode(sessionsByMode)
                .sessionsByStatus(sessionsByStatus)
                .activityByDayOfWeek(activityByDayOfWeek)
                .activityByHourOfDay(activityByHourOfDay)
                .firstQuizCreatedAt(firstQuizCreatedAt)
                .lastActivityAt(lastActivityAt)
                .memberSince(memberSince)
                .build();
    }

    private LocalDateTime getStartDateForTimeRange(String timeRange) {
        return switch (timeRange.toLowerCase()) {
            case "7days" -> LocalDateTime.now().minusDays(7);
            case "30days" -> LocalDateTime.now().minusDays(30);
            case "90days" -> LocalDateTime.now().minusDays(90);
            case "1year" -> LocalDateTime.now().minusYears(1);
            default -> LocalDateTime.of(2000, 1, 1, 0, 0); // "all"
        };
    }

    private List<Quiz> filterByTimeRange(List<Quiz> quizzes, LocalDateTime startDate) {
        return quizzes.stream()
                .filter(q -> q.getCreatedAt() != null && q.getCreatedAt().isAfter(startDate))
                .collect(Collectors.toList());
    }

    private List<QuizSession> filterSessionsByTimeRange(List<QuizSession> sessions, LocalDateTime startDate) {
        return sessions.stream()
                .filter(s -> s.getCreatedAt() != null && s.getCreatedAt().isAfter(startDate))
                .collect(Collectors.toList());
    }

    private UserActivityResponse.MostPopularQuiz getMostPopularQuiz(List<Quiz> quizzes, List<QuizSession> sessions) {
        Map<String, List<QuizSession>> sessionsByQuiz = sessions.stream()
                .filter(s -> s.getQuiz() != null)
                .collect(Collectors.groupingBy(s -> s.getQuiz().getId()));

        if (sessionsByQuiz.isEmpty()) {
            return null;
        }

        String mostPopularQuizId = sessionsByQuiz.entrySet().stream()
                .max(Comparator.comparingInt(e -> e.getValue().size()))
                .map(Map.Entry::getKey)
                .orElse(null);

        if (mostPopularQuizId == null) {
            return null;
        }

        Quiz quiz = quizzes.stream()
                .filter(q -> q.getId().equals(mostPopularQuizId))
                .findFirst()
                .orElse(null);

        if (quiz == null) {
            // Try to fetch from repository
            quiz = quizRepository.findById(mostPopularQuizId).orElse(null);
        }

        List<QuizSession> quizSessions = sessionsByQuiz.get(mostPopularQuizId);
        Integer totalSessions = quizSessions.size();
        Integer totalParticipants = quizSessions.stream()
                .mapToInt(s -> participantRepository.findBySessionId(s.getId()).size())
                .sum();

        return UserActivityResponse.MostPopularQuiz.builder()
                .quizId(mostPopularQuizId)
                .title(quiz != null ? quiz.getTitle() : "Unknown Quiz")
                .totalSessions(totalSessions)
                .totalParticipants(totalParticipants)
                .averageScore(0.0) // Calculate if you have score data
                .build();
    }

    private List<UserActivityResponse.RecentActivity> getRecentActivities(List<Quiz> quizzes, List<QuizSession> sessions) {
        List<UserActivityResponse.RecentActivity> activities = new ArrayList<>();

        // Add quiz creation activities
        quizzes.stream()
                .filter(q -> q.getCreatedAt() != null)
                .sorted(Comparator.comparing(Quiz::getCreatedAt).reversed())
                .limit(5)
                .forEach(q -> activities.add(
                        UserActivityResponse.RecentActivity.builder()
                                .activityType("QUIZ_CREATED")
                                .description("Created quiz: " + q.getTitle())
                                .timestamp(q.getCreatedAt())
                                .metadata(Map.of("quizId", q.getId(), "title", q.getTitle()))
                                .build()
                ));

        // Add session activities
        sessions.stream()
                .filter(s -> s.getCreatedAt() != null)
                .sorted(Comparator.comparing(QuizSession::getCreatedAt).reversed())
                .limit(5)
                .forEach(s -> activities.add(
                        UserActivityResponse.RecentActivity.builder()
                                .activityType(s.getStatus() == Status.COMPLETED ? "SESSION_ENDED" : "SESSION_STARTED")
                                .description("Session " + s.getSessionCode() + " - " + s.getSessionName())
                                .timestamp(s.getCreatedAt())
                                .metadata(Map.of("sessionCode", s.getSessionCode(), "status", s.getStatus().toString()))
                                .build()
                ));

        return activities.stream()
                .sorted(Comparator.comparing(UserActivityResponse.RecentActivity::timestamp).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<UserActivityResponse.TimeSeriesData> generateQuizCreationTimeSeries(List<Quiz> quizzes, String timeRange) {
        Map<LocalDate, Long> quizzesByDate = quizzes.stream()
                .filter(q -> q.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        q -> q.getCreatedAt().toLocalDate(),
                        Collectors.counting()
                ));

        return generateTimeSeriesFromMap(quizzesByDate, timeRange);
    }

    private List<UserActivityResponse.TimeSeriesData> generateSessionActivityTimeSeries(List<QuizSession> sessions, String timeRange) {
        Map<LocalDate, Long> sessionsByDate = sessions.stream()
                .filter(s -> s.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getCreatedAt().toLocalDate(),
                        Collectors.counting()
                ));

        return generateTimeSeriesFromMap(sessionsByDate, timeRange);
    }

    private List<UserActivityResponse.TimeSeriesData> generateParticipantGrowthTimeSeries(List<QuizSession> sessions, String timeRange) {
        Map<LocalDate, Integer> participantsByDate = new HashMap<>();
        
        for (QuizSession session : sessions) {
            if (session.getCreatedAt() != null) {
                LocalDate date = session.getCreatedAt().toLocalDate();
                int participantCount = participantRepository.findBySessionId(session.getId()).size();
                participantsByDate.merge(date, participantCount, Integer::sum);
            }
        }

        Map<LocalDate, Long> longMap = participantsByDate.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().longValue()));

        return generateTimeSeriesFromMap(longMap, timeRange);
    }

    private List<UserActivityResponse.TimeSeriesData> generateTimeSeriesFromMap(Map<LocalDate, Long> dataMap, String timeRange) {
        LocalDate startDate = getStartDateForTimeRange(timeRange).toLocalDate();
        LocalDate endDate = LocalDate.now();

        List<UserActivityResponse.TimeSeriesData> timeSeries = new ArrayList<>();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Long count = dataMap.getOrDefault(date, 0L);
            timeSeries.add(
                    UserActivityResponse.TimeSeriesData.builder()
                            .date(date.toString())
                            .count(count.intValue())
                            .label(date.toString())
                            .build()
            );
        }

        return timeSeries;
    }

    private Map<String, Integer> getQuizzesByCategory(List<Quiz> quizzes) {
        // Implement based on your Quiz entity structure
        // For now, returning empty map
        return new HashMap<>();
    }

    private Map<String, Integer> getQuizzesByDifficulty(List<Quiz> quizzes) {
        return quizzes.stream()
                .filter(q -> q.getDifficulty() != null)
                .collect(Collectors.groupingBy(
                        q -> q.getDifficulty().toString(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    private Map<String, Integer> getSessionsByMode(List<QuizSession> sessions) {
        return sessions.stream()
                .filter(s -> s.getMode() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getMode().toString(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    private Map<String, Integer> getSessionsByStatus(List<QuizSession> sessions) {
        return sessions.stream()
                .filter(s -> s.getStatus() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getStatus().toString(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    private Map<String, Integer> getActivityByDayOfWeek(List<Quiz> quizzes, List<QuizSession> sessions) {
        Map<String, Integer> activityMap = new HashMap<>();
        
        quizzes.stream()
                .filter(q -> q.getCreatedAt() != null)
                .forEach(q -> {
                    String dayOfWeek = q.getCreatedAt().getDayOfWeek().toString();
                    activityMap.merge(dayOfWeek, 1, Integer::sum);
                });

        sessions.stream()
                .filter(s -> s.getCreatedAt() != null)
                .forEach(s -> {
                    String dayOfWeek = s.getCreatedAt().getDayOfWeek().toString();
                    activityMap.merge(dayOfWeek, 1, Integer::sum);
                });

        return activityMap;
    }

    private Map<String, Integer> getActivityByHourOfDay(List<Quiz> quizzes, List<QuizSession> sessions) {
        Map<String, Integer> activityMap = new HashMap<>();
        
        quizzes.stream()
                .filter(q -> q.getCreatedAt() != null)
                .forEach(q -> {
                    String hour = String.format("%02d:00", q.getCreatedAt().getHour());
                    activityMap.merge(hour, 1, Integer::sum);
                });

        sessions.stream()
                .filter(s -> s.getCreatedAt() != null)
                .forEach(s -> {
                    String hour = String.format("%02d:00", s.getCreatedAt().getHour());
                    activityMap.merge(hour, 1, Integer::sum);
                });

        return activityMap;
    }

    private LocalDateTime getLastActivityTime(List<Quiz> quizzes, List<QuizSession> sessions) {
        LocalDateTime lastQuizTime = quizzes.stream()
                .map(Quiz::getCreatedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime lastSessionTime = sessions.stream()
                .map(QuizSession::getCreatedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        if (lastQuizTime == null) return lastSessionTime;
        if (lastSessionTime == null) return lastQuizTime;
        
        return lastQuizTime.isAfter(lastSessionTime) ? lastQuizTime : lastSessionTime;
    }
}

