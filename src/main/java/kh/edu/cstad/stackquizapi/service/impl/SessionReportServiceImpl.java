package kh.edu.cstad.stackquizapi.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kh.edu.cstad.stackquizapi.domain.*;
import kh.edu.cstad.stackquizapi.dto.response.LeaderboardResponse;
import kh.edu.cstad.stackquizapi.dto.response.SessionReportResponse;
import kh.edu.cstad.stackquizapi.repository.*;
import kh.edu.cstad.stackquizapi.service.LeaderboardService;
import kh.edu.cstad.stackquizapi.service.SessionReportService;
import kh.edu.cstad.stackquizapi.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionReportServiceImpl implements SessionReportService {

    private final SessionReportRepository sessionReportRepository;
    private final QuizSessionRepository quizSessionRepository;
    private final ParticipantRepository participantRepository;
    private final ParticipantAnswerRepository participantAnswerRepository;
    private final QuestionRepository questionRepository;
    private final LeaderboardService leaderboardService;
    private final ObjectMapper objectMapper;

    @Override
    public SessionReport generateReport(String sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));


        if (session.getStatus() != Status.ENDED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot generate report for session that hasn't ended");
        }


        Optional<SessionReport> existingReport = sessionReportRepository.findBySessionId(sessionId);
        if (existingReport.isPresent()) {
            log.info("Report already exists for session {}", sessionId);
            return existingReport.get();
        }

        log.info("Generating comprehensive report for session {}", sessionId);

        SessionReport report = new SessionReport();
        report.setSession(session);
        report.setGeneratedAt(LocalDateTime.now());

        // Generate all report sections
        report.setSessionOverview(generateSessionOverview(session));
        report.setQuestionBreakdown(generateQuestionBreakdown(session));
        report.setParticipantDetails(generateParticipantDetails(session));
        report.setFinalRankings(generateFinalRankings(session));

        SessionReport savedReport = sessionReportRepository.save(report);
        log.info("Successfully generated report {} for session {}", savedReport.getId(), sessionId);


        leaderboardService.finalizeSessionLeaderboard(sessionId);

        return savedReport;
    }

    @Override
    public SessionReportResponse getReport(String sessionId) {
        SessionReport report = sessionReportRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No report found for session"));

        return SessionReportResponse.builder()
                .reportId(report.getId())
                .sessionId(sessionId)
                .sessionName(report.getSession().getSessionName())
                .hostName(report.getSession().getHostName())
                .generatedAt(report.getGeneratedAt())
                .sessionOverview(report.getSessionOverview())
                .questionBreakdown(report.getQuestionBreakdown())
                .participantDetails(report.getParticipantDetails())
                .finalRankings(report.getFinalRankings())
                .build();
    }

    @Override
    public List<SessionReportResponse> getHostReports(String hostId) {
        List<SessionReport> reports = sessionReportRepository.findBySessionHostIdOrderByGeneratedAtDesc(hostId);

        return reports.stream()
                .map(report -> SessionReportResponse.builder()
                        .reportId(report.getId())
                        .sessionId(report.getSession().getId())
                        .sessionName(report.getSession().getSessionName())
                        .hostName(report.getSession().getHostName())
                        .generatedAt(report.getGeneratedAt())
                        .sessionOverview(report.getSessionOverview())
                        .questionBreakdown(report.getQuestionBreakdown())
                        .participantDetails(report.getParticipantDetails())
                        .finalRankings(report.getFinalRankings())
                        .build())
                .collect(Collectors.toList());
    }

    private JsonNode generateSessionOverview(QuizSession session) {
        ObjectNode overview = objectMapper.createObjectNode();

        // Basic session info
        overview.put("sessionId", session.getId());
        overview.put("sessionName", session.getSessionName());
        overview.put("sessionCode", session.getSessionCode());
        overview.put("hostName", session.getHostName());
        overview.put("quizTitle", session.getQuiz().getTitle());

        // Timing information
        overview.put("startTime", session.getStartTime().toString());
        overview.put("endTime", session.getEndTime().toString());

        Duration sessionDuration = Duration.between(session.getStartTime(), session.getEndTime());
        overview.put("durationMinutes", sessionDuration.toMinutes());
        overview.put("durationSeconds", sessionDuration.getSeconds());

        // Participation statistics
        List<Participant> participants = participantRepository.findBySessionId(session.getId());
        overview.put("totalParticipants", participants.size());
        overview.put("activeParticipants", participants.stream().mapToInt(p -> p.getIsActive() ? 1 : 0).sum());

        // Question statistics
        overview.put("totalQuestions", session.getTotalQuestions());
        overview.put("currentQuestionReached", session.getCurrentQuestion());

        // Answer statistics
        List<ParticipantAnswer> allAnswers = participantAnswerRepository.findByParticipantSessionId(session.getId());
        long correctAnswers = allAnswers.stream().mapToLong(a -> a.getIsCorrect() ? 1L : 0L).sum();
        overview.put("totalAnswers", allAnswers.size());
        overview.put("correctAnswers", correctAnswers);
        overview.put("accuracyRate", allAnswers.size() > 0 ? (double) correctAnswers / allAnswers.size() * 100 : 0.0);

        // Score statistics
        OptionalDouble avgScore = participants.stream().mapToInt(Participant::getTotalScore).average();
        OptionalInt maxScore = participants.stream().mapToInt(Participant::getTotalScore).max();
        OptionalInt minScore = participants.stream().mapToInt(Participant::getTotalScore).min();

        overview.put("averageScore", avgScore.orElse(0.0));
        overview.put("highestScore", maxScore.orElse(0));
        overview.put("lowestScore", minScore.orElse(0));

        // Completion rate
        long expectedAnswers = (long) participants.size() * session.getTotalQuestions();
        double completionRate = expectedAnswers > 0 ? (double) allAnswers.size() / expectedAnswers * 100 : 0.0;
        overview.put("completionRate", completionRate);

        return overview;
    }

    private JsonNode generateQuestionBreakdown(QuizSession session) {
        ArrayNode questionArray = objectMapper.createArrayNode();

        List<Question> questions = session.getQuiz().getQuestions().stream()
                .sorted(Comparator.comparing(Question::getQuestionOrder))
                .toList();

        for (Question question : questions) {
            ObjectNode questionNode = objectMapper.createObjectNode();

            questionNode.put("questionId", question.getId());
            questionNode.put("questionOrder", question.getQuestionOrder());
            questionNode.put("questionText", question.getText());
            questionNode.put("questionType", question.getType().toString());
            questionNode.put("timeLimit", question.getTimeLimit());
            questionNode.put("maxPoints", question.getPoints());

            List<ParticipantAnswer> questionAnswers = participantAnswerRepository
                    .findByQuestionIdAndParticipantSessionId(question.getId(), session.getId());

            questionNode.put("totalResponses", questionAnswers.size());

            long correctCount = questionAnswers.stream().mapToLong(a -> a.getIsCorrect() ? 1L : 0L).sum();
            questionNode.put("correctAnswers", correctCount);
            questionNode.put("incorrectAnswers", questionAnswers.size() - correctCount);

            double accuracyRate = questionAnswers.size() > 0 ? (double) correctCount / questionAnswers.size() * 100 : 0.0;
            questionNode.put("accuracyRate", accuracyRate);

            // Average time taken
            OptionalDouble avgTime = questionAnswers.stream().mapToInt(ParticipantAnswer::getTimeTaken).average();
            questionNode.put("averageTimeSeconds", avgTime.orElse(0.0));

            // Points distribution
            OptionalDouble avgPoints = questionAnswers.stream().mapToInt(ParticipantAnswer::getPointsEarned).average();
            OptionalInt maxPoints = questionAnswers.stream().mapToInt(ParticipantAnswer::getPointsEarned).max();
            questionNode.put("averagePointsEarned", avgPoints.orElse(0.0));
            questionNode.put("maxPointsEarned", maxPoints.orElse(0));

            // Answer option breakdown
            ArrayNode optionsArray = objectMapper.createArrayNode();
            for (Option option : question.getOptions()) {
                ObjectNode optionNode = objectMapper.createObjectNode();
                optionNode.put("Id", option.getId());
                optionNode.put("optionText", option.getOptionText());
                optionNode.put("isCorrect", option.getIsCorrected());

                long selectedCount = questionAnswers.stream()
                        .filter(a -> a.getSelectedAnswer() != null)
                        .mapToLong(a -> a.getSelectedAnswer().getId().equals(option.getId()) ? 1L : 0L)
                        .sum();
                optionNode.put("selectedCount", selectedCount);

                double selectionRate = !questionAnswers.isEmpty() ? (double) selectedCount / questionAnswers.size() * 100 : 0.0;
                optionNode.put("selectionRate", selectionRate);

                optionsArray.add(optionNode);
            }
            questionNode.set("optionBreakdown", optionsArray);

            questionArray.add(questionNode);
        }

        return questionArray;
    }

    private JsonNode generateParticipantDetails(QuizSession session) {
        ArrayNode participantArray = objectMapper.createArrayNode();

        List<Participant> participants = participantRepository.findBySessionIdOrderByTotalScoreDesc(session.getId());

        int position = 1;
        for (Participant participant : participants) {
            ObjectNode participantNode = objectMapper.createObjectNode();

            participantNode.put("participantId", participant.getId());
            participantNode.put("nickname", participant.getNickname());
            participantNode.put("finalPosition", position);
            participantNode.put("totalScore", participant.getTotalScore());
            participantNode.put("joinedAt", participant.getJoinedAt().toString());
            participantNode.put("isActive", participant.getIsActive());

            // Get participant's answers
            List<ParticipantAnswer> participantAnswers = participantAnswerRepository
                    .findByParticipantIdOrderByAnsweredAt(participant.getId());

            participantNode.put("totalAnswers", participantAnswers.size());

            long correctAnswers = participantAnswers.stream().mapToLong(a -> a.getIsCorrect() ? 1L : 0L).sum();
            participantNode.put("correctAnswers", correctAnswers);
            participantNode.put("incorrectAnswers", participantAnswers.size() - correctAnswers);

            double accuracy = !participantAnswers.isEmpty() ? (double) correctAnswers / participantAnswers.size() * 100 : 0.0;
            participantNode.put("accuracy", accuracy);

            // Average response time
            OptionalDouble avgResponseTime = participantAnswers.stream()
                    .mapToInt(ParticipantAnswer::getTimeTaken)
                    .average();
            participantNode.put("averageResponseTime", avgResponseTime.orElse(0.0));

            // Question-by-question performance
            ArrayNode answersArray = objectMapper.createArrayNode();
            for (ParticipantAnswer answer : participantAnswers) {
                ObjectNode answerNode = objectMapper.createObjectNode();
                answerNode.put("questionId", answer.getQuestion().getId());
                answerNode.put("questionOrder", answer.getQuestion().getQuestionOrder());
                answerNode.put("selectedAnswerId", answer.getSelectedAnswer() != null ? answer.getSelectedAnswer().getId() : null);
                answerNode.put("isCorrect", answer.getIsCorrect());
                answerNode.put("pointsEarned", answer.getPointsEarned());
                answerNode.put("timeTaken", answer.getTimeTaken());
                answerNode.put("answeredAt", answer.getAnsweredAt().toString());
                answersArray.add(answerNode);
            }
            participantNode.set("questionAnswers", answersArray);

            participantArray.add(participantNode);
            position++;
        }

        return participantArray;
    }

    private JsonNode generateFinalRankings(QuizSession session) {

        LeaderboardResponse leaderboard = leaderboardService.getPodium(session.getId());
        ObjectNode rankings = objectMapper.createObjectNode();

        // Podium (Top 3)
        ArrayNode podium = objectMapper.createArrayNode();
        for (LeaderboardResponse.LeaderboardEntry entry : leaderboard.entries()) {
            ObjectNode podiumEntry = objectMapper.createObjectNode();
            podiumEntry.put("position", entry.position());
            podiumEntry.put("participantId", entry.participantId());
            podiumEntry.put("nickname", entry.nickname());
            podiumEntry.put("totalScore", entry.totalScore());
            podiumEntry.put("badge", getBadgeForPosition(entry.position()));
            podium.add(podiumEntry);
        }
        rankings.set("podium", podium);

        // Full leaderboard from database as fallback
        ArrayNode fullLeaderboard = objectMapper.createArrayNode();
        List<Participant> participants = participantRepository.findBySessionIdOrderByTotalScoreDesc(session.getId());
        for (int i = 0; i < participants.size(); i++) {
            Participant p = participants.get(i);
            ObjectNode entry = objectMapper.createObjectNode();
            entry.put("position", i + 1);
            entry.put("participantId", p.getId());
            entry.put("nickname", p.getNickname());
            entry.put("totalScore", p.getTotalScore());
            entry.put("badge", getBadgeForPosition(i + 1));
            fullLeaderboard.add(entry);
        }
        rankings.set("fullLeaderboard", fullLeaderboard);

        // Rankings metadata
        rankings.put("totalParticipants", participants.size());
        rankings.put("generatedAt", LocalDateTime.now().toString());

        return rankings;
    }

    private String getBadgeForPosition(int position) {
        return switch (position) {
            case 1 -> "🥇";
            case 2 -> "🥈";
            case 3 -> "🥉";
            default -> null;
        };
    }
}