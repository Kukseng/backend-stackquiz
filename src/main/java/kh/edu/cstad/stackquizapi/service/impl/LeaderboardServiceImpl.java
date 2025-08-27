package kh.edu.cstad.stackquizapi.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kh.edu.cstad.stackquizapi.domain.*;
import kh.edu.cstad.stackquizapi.dto.request.HistoricalLeaderboardRequest;
import kh.edu.cstad.stackquizapi.dto.request.LeaderboardRequest;
import kh.edu.cstad.stackquizapi.dto.response.*;
import kh.edu.cstad.stackquizapi.repository.*;
import kh.edu.cstad.stackquizapi.service.LeaderboardService;
import kh.edu.cstad.stackquizapi.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
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
public class LeaderboardServiceImpl implements LeaderboardService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final QuizSessionRepository quizSessionRepository;
    private final ParticipantRepository participantRepository;
    private final ParticipantAnswerRepository participantAnswerRepository;
    private final QuestionRepository questionRepository;

    private static final String LEADERBOARD_KEY_PREFIX = "leaderboard:session:";
    private static final String PARTICIPANT_DATA_PREFIX = "participant:";

    @Override
    public LeaderboardResponse getRealTimeLeaderboard(LeaderboardRequest request) {

        String actualSessionId = request.sessionId();


        if (request.sessionId().length() <= 10) {
            QuizSession session = quizSessionRepository.findBySessionCode(request.sessionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Session not found"));
            actualSessionId = session.getId();
        }

        String leaderboardKey = LEADERBOARD_KEY_PREFIX + actualSessionId;





        long start = request.offset();
        long end = start + request.limit() - 1;

        Set<ZSetOperations.TypedTuple<String>> participantTuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(leaderboardKey, start, end);

        List<LeaderboardResponse.LeaderboardEntry> entries = new ArrayList<>();

        if (participantTuples != null) {
            int position = request.offset() + 1;
            for (ZSetOperations.TypedTuple<String> tuple : participantTuples) {
                String participantKey = tuple.getValue();
                Double score = tuple.getScore();

                if (participantKey != null && score != null) {
                    String participantJson = redisTemplate.opsForValue()
                            .get(participantKey + ":" + actualSessionId);

                    if (participantJson != null) {
                        try {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> participantData = objectMapper.readValue(
                                    participantJson, Map.class);

                            String participantId = (String) participantData.get("participantId");
                            String nickname = (String) participantData.get("nickname");

                            LeaderboardResponse.LeaderboardEntry entry =
                                    LeaderboardResponse.LeaderboardEntry.builder()
                                            .participantId(participantId)
                                            .nickname(nickname)
                                            .totalScore(score.intValue())
                                            .position(position)

                                            .rank((long) position)
                                            .isCurrentUser(participantId.equals(request.currentParticipantId()))
                                            .build();

                            entries.add(entry);
                            position++;
                        } catch (JsonProcessingException e) {
                            log.error("Error parsing participant data from Redis", e);
                        }
                    }
                }
            }
        }


        int totalParticipants = (int) participantRepository.countBySessionIdAndIsActiveTrue(actualSessionId);

        String leaderboardStatus = determineLeaderboardStatus(actualSessionId);

        return LeaderboardResponse.builder()
                .sessionId(request.sessionId())
                .entries(entries)
                .totalParticipants(totalParticipants)
                .lastUpdated(System.currentTimeMillis())
                .status(leaderboardStatus)
                .build();
    }

    @Override
    public ParticipantRankResponse getParticipantRank(String sessionId, String participantId) {

        String actualSessionId = sessionId;

        if (sessionId.length() <= 10) {
            QuizSession session = quizSessionRepository.findBySessionCode(sessionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Session not found"));
            actualSessionId = session.getId();
        }

        String leaderboardKey = LEADERBOARD_KEY_PREFIX + actualSessionId;
        String participantKey = PARTICIPANT_DATA_PREFIX + participantId;


        Long rank = redisTemplate.opsForZSet().reverseRank(leaderboardKey, participantKey);

        // Get participant's score
        Double score = redisTemplate.opsForZSet().score(leaderboardKey, participantKey);

        // Get participant details
        String participantJson = redisTemplate.opsForValue()
                .get(participantKey + ":" + actualSessionId);

        log.info("Redis data - Rank: {}, Score: {}, ParticipantJson: {}", rank, score, participantJson != null ? "found" : "null");

        String nickname = "Unknown";
        if (participantJson != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> participantData = objectMapper.readValue(
                        participantJson, Map.class);
                nickname = (String) participantData.get("nickname");
            } catch (JsonProcessingException e) {
                log.error("Error parsing participant data", e);
            }
        }


        int totalParticipants = (int) participantRepository.countBySessionIdAndIsActiveTrue(actualSessionId);

        log.info("Final result - Nickname: {}, Rank: {}, Score: {}, TotalParticipants: {}",
                nickname, rank, score, totalParticipants);

        return new ParticipantRankResponse(
                participantId,
                nickname,
                score != null ? score.intValue() : 0,
                rank != null ? rank + 1 : null,
                totalParticipants,
                sessionId,
                System.currentTimeMillis()
        );
    }

    @Override
    public LeaderboardResponse getPodium(String sessionId) {
        LeaderboardRequest request = new LeaderboardRequest(sessionId, 3, 0, false, null);
        LeaderboardResponse response = getRealTimeLeaderboard(request);

        return LeaderboardResponse.builder()
                .sessionId(response.sessionId())
                .entries(response.entries())
                .totalParticipants(response.totalParticipants())
                .lastUpdated(response.lastUpdated())
                .status("PODIUM")
                .build();
    }

    @Override
    public void updateParticipantScore(String sessionId, String participantId, String nickname, int newScore) {
        String leaderboardKey = LEADERBOARD_KEY_PREFIX + sessionId;
        String participantKey = PARTICIPANT_DATA_PREFIX + participantId;

        try {
            // Store participant data
            Map<String, Object> participantData = Map.of(
                    "participantId", participantId,
                    "nickname", nickname,
                    "score", newScore,
                    "lastUpdated", System.currentTimeMillis()
            );

            String participantJson = objectMapper.writeValueAsString(participantData);

            // Update score in sorted set
            redisTemplate.opsForZSet().add(leaderboardKey, participantKey, newScore);

            // Store participant details
            redisTemplate.opsForValue().set(participantKey + ":" + sessionId, participantJson);

            // Set TTL
            redisTemplate.expire(leaderboardKey, java.time.Duration.ofHours(24));
            redisTemplate.expire(participantKey + ":" + sessionId, java.time.Duration.ofHours(24));

            log.debug("Updated participant {} score to {} in session {}", nickname, newScore, sessionId);

        } catch (JsonProcessingException e) {
            log.error("Error updating participant score in Redis", e);
        }
    }

    @Override
    public void removeParticipant(String sessionId, String participantId) {
        String leaderboardKey = LEADERBOARD_KEY_PREFIX + sessionId;
        String participantKey = PARTICIPANT_DATA_PREFIX + participantId;

        redisTemplate.opsForZSet().remove(leaderboardKey, participantKey);
        redisTemplate.delete(participantKey + ":" + sessionId);

        log.debug("Removed participant {} from session {}", participantId, sessionId);
    }

    @Override
    public List<HistoricalLeaderboardResponse> getHistoricalLeaderboards(HistoricalLeaderboardRequest request) {
        Pageable pageable = PageRequest.of(request.page() - 1, request.size());

        List<QuizSession> sessions;

        if (request.sessionId() != null) {

            sessions = quizSessionRepository.findById(request.sessionId())
                    .map(List::of)
                    .orElse(List.of());
        } else {

            if (request.hostId() != null) {
                sessions = quizSessionRepository.findByHostIdAndStatusOrderByCreatedAtDesc(
                        request.hostId(), Status.ENDED, pageable).getContent();
            } else {
                sessions = quizSessionRepository.findByStatusOrderByCreatedAtDesc(
                        Status.ENDED, pageable).getContent();
            }

            // Apply date filters if provided
            if (request.startDate() != null || request.endDate() != null) {
                sessions = sessions.stream()
                        .filter(session -> {
                            LocalDateTime sessionTime = session.getEndTime();
                            if (sessionTime == null) return false;

                            boolean afterStart = request.startDate() == null ||
                                    sessionTime.isAfter(request.startDate());
                            boolean beforeEnd = request.endDate() == null ||
                                    sessionTime.isBefore(request.endDate());

                            return afterStart && beforeEnd;
                        })
                        .collect(Collectors.toList());
            }
        }

        return sessions.stream()
                .map(this::convertToHistoricalResponse)
                .collect(Collectors.toList());
    }

    @Override
    public HistoricalLeaderboardResponse getSessionReport(String sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        return convertToHistoricalResponse(session);
    }

    @Override
    public void initializeSessionLeaderboard(String sessionId) {

        List<Participant> participants = participantRepository.findBySessionIdAndIsActiveTrue(sessionId);

        for (Participant participant : participants) {
            updateParticipantScore(sessionId, participant.getId(),
                    participant.getNickname(), participant.getTotalScore());
        }

        log.info("Initialized leaderboard for session {} with {} participants",
                sessionId, participants.size());
    }

    @Override
    public void finalizeSessionLeaderboard(String sessionId) {

        String leaderboardKey = LEADERBOARD_KEY_PREFIX + sessionId;
        redisTemplate.expire(leaderboardKey, java.time.Duration.ofDays(7)); // Keep for 7 days

        // Get all participant keys and extend their TTL too
        Set<String> participantKeys = redisTemplate.opsForZSet().range(leaderboardKey, 0, -1);
        if (participantKeys != null) {
            participantKeys.forEach(key ->
                    redisTemplate.expire(key + ":" + sessionId, java.time.Duration.ofDays(7)));
        }

        log.info("Finalized leaderboard for session {} - extended TTL to 7 days", sessionId);
    }

    @Override
    public void clearSessionLeaderboard(String sessionId) {
        String leaderboardKey = LEADERBOARD_KEY_PREFIX + sessionId;

        // Get all participant keys first
        Set<String> participantKeys = redisTemplate.opsForZSet().range(leaderboardKey, 0, -1);

        if (participantKeys != null) {
            participantKeys.forEach(key -> redisTemplate.delete(key + ":" + sessionId));
        }

        redisTemplate.delete(leaderboardKey);

        log.info("Cleared leaderboard for session {}", sessionId);
    }

    @Override
    public SessionStats getSessionStatistics(String sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        List<Participant> participants = participantRepository.findBySessionId(sessionId);
        List<ParticipantAnswer> answers = participantAnswerRepository.findByParticipantSessionId(sessionId);

        if (participants.isEmpty()) {
            return SessionStats.builder()
                    .totalQuestions(session.getTotalQuestions() != null ? session.getTotalQuestions() : 0)
                    .averageScore(0.0)
                    .completionRate(0.0)
                    .duration("PT0S")
                    .totalParticipants(0)
                    .highestScore(0.0)
                    .lowestScore(0.0)
                    .build();
        }

        double averageScore = participants.stream()
                .mapToInt(Participant::getTotalScore)
                .average()
                .orElse(0.0);

        int highestScore = participants.stream()
                .mapToInt(Participant::getTotalScore)
                .max()
                .orElse(0);

        int lowestScore = participants.stream()
                .mapToInt(Participant::getTotalScore)
                .min()
                .orElse(0);

        // Calculate completion rate
        int totalQuestions = session.getTotalQuestions() != null ? session.getTotalQuestions() : 0;
        long expectedAnswers = (long) participants.size() * totalQuestions;
        double completionRate = expectedAnswers > 0 ? (double) answers.size() / expectedAnswers * 100 : 0;


        String duration = "Time";
        if (session.getStartTime() != null && session.getEndTime() != null) {
            Duration sessionDuration = Duration.between(session.getStartTime(), session.getEndTime());
            duration = sessionDuration.toString();
        }

        return SessionStats.builder()
                .totalQuestions(totalQuestions)
                .averageScore(Math.round(averageScore * 100.0) / 100.0)
                .completionRate(Math.round(completionRate * 100.0) / 100.0)
                .duration(duration)
                .totalParticipants(participants.size())
                .highestScore(highestScore)
                .lowestScore(lowestScore)
                .build();
    }

    private int getTotalParticipants(String sessionId) {

        String actualSessionId = sessionId;

        if (sessionId.length() <= 10) {
            QuizSession session = quizSessionRepository.findBySessionCode(sessionId)
                    .orElse(null);
            if (session != null) {
                actualSessionId = session.getId();
            }
        }

        // Try Redis first
        String leaderboardKey = LEADERBOARD_KEY_PREFIX + actualSessionId;
        Long redisCount = redisTemplate.opsForZSet().zCard(leaderboardKey);

        if (redisCount != null && redisCount > 0) {
            return redisCount.intValue();
        }

        // Fallback to database
        return (int) participantRepository.countBySessionIdAndIsActiveTrue(actualSessionId);
    }

    private String getBadge(int position) {
        return switch (position) {
            case 1 -> "🥇";
            case 2 -> "🥈";
            case 3 -> "🥉";
            default -> null;
        };
    }

    private HistoricalLeaderboardResponse convertToHistoricalResponse(QuizSession session) {
        LeaderboardResponse finalLeaderboard = null;


        if (session.getLeaderboardData() != null) {
            finalLeaderboard = convertJsonToLeaderboardResponse(session.getLeaderboardData(), session.getId());
        } else {
            // Fallback: generate from database
            List<Participant> participants = participantRepository
                    .findBySessionIdOrderByTotalScoreDesc(session.getId());

            List<LeaderboardResponse.LeaderboardEntry> entries = new ArrayList<>();
            for (int i = 0; i < participants.size(); i++) {
                Participant p = participants.get(i);
                entries.add(LeaderboardResponse.LeaderboardEntry.builder()
                        .participantId(p.getId())
                        .nickname(p.getNickname())
                        .totalScore(p.getTotalScore())
                        .position(i + 1)

                        .rank((long) i + 1)
                        .isCurrentUser(false)
                        .build());
            }

            finalLeaderboard = LeaderboardResponse.builder()
                    .sessionId(session.getId())
                    .entries(entries)
                    .totalParticipants(participants.size())
                    .lastUpdated(System.currentTimeMillis())
                    .status("FINAL")
                    .build();
        }

        return HistoricalLeaderboardResponse.builder()
                .sessionId(session.getId())
                .sessionName(session.getSessionName())
                .hostName(session.getHostName())
                .sessionEndTime(session.getEndTime())
                .finalLeaderboard(finalLeaderboard)
                .stats(getSessionStatistics(session.getId()))
                .build();
    }

    private String determineLeaderboardStatus(String sessionId) {
        try {
            QuizSession session = quizSessionRepository.findById(sessionId).orElse(null);

            if (session == null) {
                return "ERROR";
            }

            return switch (session.getStatus()) {
                case LIVE, WAITING, IN_PROGRESS -> "LIVE";
                case ENDED, COMPLETED -> "FINAL";
//                case PAUSED -> "PAUSED";
//                case CANCELLED -> "CANCELLED";
                default -> "UNKNOWN";
            };

        } catch (Exception e) {
            log.error("Error determining session status", e);
            return "ERROR";
        }
    }

    private LeaderboardResponse convertJsonToLeaderboardResponse(JsonNode leaderboardJson, String sessionId) {
        try {

            LeaderboardResponse response = objectMapper.treeToValue(leaderboardJson, LeaderboardResponse.class);
            return LeaderboardResponse.builder()
                    .sessionId(sessionId)
                    .entries(response.entries())
                    .totalParticipants(response.totalParticipants())
                    .lastUpdated(response.lastUpdated())
                    .status("FINAL")
                    .build();
        } catch (JsonProcessingException e) {
            log.error("Error converting JSONB to LeaderboardResponse", e);
            return LeaderboardResponse.builder()
                    .sessionId(sessionId)
                    .entries(List.of())
                    .totalParticipants(participantRepository.countBySessionId(sessionId))
                    .lastUpdated(System.currentTimeMillis())
                    .status("ERROR")
                    .build();
        }
    }
}
