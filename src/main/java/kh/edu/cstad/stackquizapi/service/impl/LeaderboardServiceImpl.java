package kh.edu.cstad.stackquizapi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import kh.edu.cstad.stackquizapi.domain.*;
import kh.edu.cstad.stackquizapi.dto.request.HistoricalLeaderboardRequest;
import kh.edu.cstad.stackquizapi.dto.request.LeaderboardRequest;
import kh.edu.cstad.stackquizapi.dto.request.ParticipantRedisData;
import kh.edu.cstad.stackquizapi.dto.response.*;
import kh.edu.cstad.stackquizapi.dto.websocket.LeaderboardMessage;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
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
    //    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private static final String LEADERBOARD_KEY_PREFIX = "leaderboard:session:";
    private static final String PARTICIPANT_DATA_PREFIX = "participant:";

    private String leaderboardKey(String sessionId) {
        return "leaderboard:" + sessionId;
    }

    @Override
    public LeaderboardResponse getRealTimeLeaderboard(LeaderboardRequest request) {
        log.info("Getting real-time leaderboard for session: {}", request.sessionId());

        String actualSessionId = resolveSessionId(request.sessionId());
        String leaderboardKey = LEADERBOARD_KEY_PREFIX + actualSessionId;

        log.info("Using session ID: {} for leaderboard key: {}", actualSessionId, leaderboardKey);

        long start = request.offset();
        long end = start + request.limit() - 1;

        Set<ZSetOperations.TypedTuple<String>> participantTuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(leaderboardKey, start, end);

        List<LeaderboardResponse.LeaderboardEntry> entries = new ArrayList<>();

        if (participantTuples != null && !participantTuples.isEmpty()) {
            log.info("Found {} participants in Redis leaderboard", participantTuples.size());

            int position = request.offset() + 1;
            for (ZSetOperations.TypedTuple<String> tuple : participantTuples) {
                String participantKey = tuple.getValue();
                Double score = tuple.getScore();

                if (participantKey != null && score != null) {
                    String participantDataKey = participantKey + ":" + actualSessionId;
                    String participantJson = redisTemplate.opsForValue().get(participantDataKey);

                    log.debug("Participant key: {}, Score: {}, Data found: {}",
                            participantKey, score, participantJson != null);

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
        } else {
            log.warn("No participants found in Redis leaderboard for session: {}", actualSessionId);

            // Fallback to database if Redis is empty
            List<Participant> dbParticipants = participantRepository.findBySessionIdAndIsActiveTrueOrderByTotalScoreDesc(actualSessionId);
            log.info("Fallback: Found {} participants in database", dbParticipants.size());

            int position = 1;
            for (Participant participant : dbParticipants) {
                LeaderboardResponse.LeaderboardEntry entry =
                        LeaderboardResponse.LeaderboardEntry.builder()
                                .participantId(participant.getId())
                                .nickname(participant.getNickname())
                                .totalScore(participant.getTotalScore())
                                .position(position)
                                .rank((long) position)
                                .isCurrentUser(participant.getId().equals(request.currentParticipantId()))
                                .build();
                entries.add(entry);
                position++;
            }
        }

        int totalParticipants = (int) participantRepository.countBySessionIdAndIsActiveTrue(actualSessionId);
        String leaderboardStatus = determineLeaderboardStatus(actualSessionId);

        log.info("Returning leaderboard with {} entries, total participants: {}", entries.size(), totalParticipants);

        return LeaderboardResponse.builder()
                .sessionId(request.sessionId()) // Return original session identifier
                .entries(entries)
                .totalParticipants(totalParticipants)
                .lastUpdated(System.currentTimeMillis())
                .status(leaderboardStatus)
                .build();
    }

    @Override
    public ParticipantRankResponse getParticipantRank(String sessionId, String participantId) {
        log.info("Getting participant rank for participant: {} in session: {}", participantId, sessionId);

        String actualSessionId = resolveSessionId(sessionId);
        String leaderboardKey = LEADERBOARD_KEY_PREFIX + actualSessionId;
        String participantKey = PARTICIPANT_DATA_PREFIX + participantId;

        Long rank = redisTemplate.opsForZSet().reverseRank(leaderboardKey, participantKey);
        Double score = redisTemplate.opsForZSet().score(leaderboardKey, participantKey);
        String participantJson = redisTemplate.opsForValue().get(participantKey + ":" + actualSessionId);

        log.info("Redis data - Rank: {}, Score: {}, ParticipantJson: {}", rank, score, participantJson != null ? "found" : "null");

        String nickname = "Unknown";
        if (participantJson != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> participantData = objectMapper.readValue(participantJson, Map.class);
                nickname = (String) participantData.get("nickname");
            } catch (JsonProcessingException e) {
                log.error("Error parsing participant data", e);
            }
        } else {
            // Fallback to database
            Participant participant = participantRepository.findById(participantId).orElse(null);
            if (participant != null) {
                nickname = participant.getNickname();
                score = (double) participant.getTotalScore();
                log.info("Fallback to database - Nickname: {}, Score: {}", nickname, score);
            }
        }

        int totalParticipants = (int) participantRepository.countBySessionIdAndIsActiveTrue(actualSessionId);

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
        String key = leaderboardKey(sessionId);

        // Store participant data in Redis using the correct key structure
        String participantKey = PARTICIPANT_DATA_PREFIX + participantId;
        String participantDataKey = participantKey + ":" + sessionId;

        // Create participant data map
        Map<String, Object> participantData = new HashMap<>();
        participantData.put("participantId", participantId);
        participantData.put("nickname", nickname);
        participantData.put("totalScore", newScore);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String participantJson = objectMapper.writeValueAsString(participantData);
            redisTemplate.opsForValue().set(participantDataKey, participantJson);

            // Also update the ZSet for leaderboard ranking
            redisTemplate.opsForZSet().add(leaderboardKey(sessionId), participantKey, newScore);

            log.info("Updated participant {} score to {} in session {}", participantId, newScore, sessionId);
        } catch (Exception e) {
            log.error("Failed to serialize participant data to JSON: {}", e.getMessage());
            // Fallback: still update the ZSet for ranking
            redisTemplate.opsForZSet().add(leaderboardKey(sessionId), participantKey, newScore);
        }


        LeaderboardResponse leaderboard = getRealTimeLeaderboard(
                new LeaderboardRequest(sessionId, 100, 0, false, null)
        );


        LeaderboardMessage message = new LeaderboardMessage(
                sessionId,
                nickname,
                leaderboard,
                "SCORE_UPDATE"
        );

        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/leaderboard", message);
    }


//





    //
    @Override
    public void removeParticipant(String sessionId, String participantId) {
        String actualSessionId = resolveSessionId(sessionId);
        String leaderboardKey = LEADERBOARD_KEY_PREFIX + actualSessionId;
        String participantKey = PARTICIPANT_DATA_PREFIX + participantId;

        redisTemplate.opsForZSet().remove(leaderboardKey, participantKey);
        redisTemplate.delete(participantKey + ":" + actualSessionId);

        log.info("Removed participant {} from session {}", participantId, actualSessionId);
    }

    @Override
    public List<HistoricalLeaderboardResponse> getHistoricalLeaderboards(HistoricalLeaderboardRequest request, Jwt accessToken) {
        String hostId = accessToken.getSubject();
        Pageable pageable = PageRequest.of(request.page() - 1, request.size());

        List<QuizSession> sessions;

        if (request.sessionId() != null) {
            sessions = quizSessionRepository.findById(request.sessionId())
                    .map(List::of)
                    .orElse(List.of());
        } else {
            if (hostId != null) {
                sessions = quizSessionRepository.findByHostIdAndStatusOrderByCreatedAtDesc(
                        hostId, Status.ENDED, pageable).getContent();
            } else {
                sessions = quizSessionRepository.findByStatusOrderByCreatedAtDesc(
                        Status.ENDED, pageable).getContent();
            }

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
        log.info("Initializing leaderboard for session: {}", sessionId);

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
        String actualSessionId = resolveSessionId(sessionId);
        String leaderboardKey = LEADERBOARD_KEY_PREFIX + actualSessionId;

        redisTemplate.expire(leaderboardKey, Duration.ofDays(7));

        Set<String> participantKeys = redisTemplate.opsForZSet().range(leaderboardKey, 0, -1);
        if (participantKeys != null) {
            participantKeys.forEach(key ->
                    redisTemplate.expire(key + ":" + actualSessionId, Duration.ofDays(7)));
        }

        log.info("Finalized leaderboard for session {} - extended TTL to 7 days", actualSessionId);
    }

    @Override
    public void clearSessionLeaderboard(String sessionId) {
        String actualSessionId = resolveSessionId(sessionId);
        String leaderboardKey = LEADERBOARD_KEY_PREFIX + actualSessionId;

        Set<String> participantKeys = redisTemplate.opsForZSet().range(leaderboardKey, 0, -1);

        if (participantKeys != null) {
            participantKeys.forEach(key -> redisTemplate.delete(key + ":" + actualSessionId));
        }

        redisTemplate.delete(leaderboardKey);

        log.info("Cleared leaderboard for session {}", actualSessionId);
    }

    @Override
    public SessionStats getSessionStatistics(String sessionId) {
        String actualSessionId = resolveSessionId(sessionId);
        QuizSession session = quizSessionRepository.findById(actualSessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        List<Participant> participants = participantRepository.findBySessionId(actualSessionId);
        List<ParticipantAnswer> answers = participantAnswerRepository.findByParticipantSessionId(actualSessionId);

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

        int totalQuestions = session.getTotalQuestions() != null ? session.getTotalQuestions() : 0;
        long expectedAnswers = (long) participants.size() * totalQuestions;
        double completionRate = expectedAnswers > 0 ? (double) answers.size() / expectedAnswers * 100 : 0;

        String duration = "PT0S";
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
                .highestScore((double) highestScore)
                .lowestScore((double) lowestScore)
                .build();
    }

    /**
     * Resolves session identifier to actual session ID
     * If the identifier is a session code (short), converts it to session ID
     * If it's already a session ID (UUID), returns as is
     */
    private String resolveSessionId(String sessionIdentifier) {
        if (sessionIdentifier == null) {
            throw new IllegalArgumentException("Session identifier cannot be null");
        }

        // If it looks like a session code (short string), convert to session ID
        if (sessionIdentifier.length() <= 10) {
            QuizSession session = quizSessionRepository.findBySessionCode(sessionIdentifier)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Session not found for code: " + sessionIdentifier));
            return session.getId();
        }

        // Otherwise, assume it's already a session ID
        return sessionIdentifier;
    }






    private String determineLeaderboardStatus(String sessionId) {
        try {
            QuizSession session = quizSessionRepository.findById(sessionId).orElse(null);
            if (session == null) return "UNKNOWN";

            return switch (session.getStatus()) {
                case WAITING -> "LOBBY";
                case IN_PROGRESS -> "LIVE";
                case ENDED -> "FINAL";
                case PAUSED -> "PAUSED";
                default -> "UNKNOWN";
            };
        } catch (Exception e) {
            log.error("Error determining leaderboard status", e);
            return "ERROR";
        }
    }

    private HistoricalLeaderboardResponse convertToHistoricalResponse(QuizSession session) {
        try {
            LeaderboardResponse currentLeaderboard = getRealTimeLeaderboard(
                    new LeaderboardRequest(session.getId(), 100, 0, false, null)
            );

            return HistoricalLeaderboardResponse.builder()
                    .sessionId(session.getId())
                    .sessionName(session.getSessionName())
                    .sessionCode(session.getSessionCode())
                    .hostName(session.getHostName())
                    .startTime(session.getStartTime())
                    .endTime(session.getEndTime())
                    .totalParticipants(session.getTotalParticipants())
                    .totalQuestions(session.getTotalQuestions())
                    .status(session.getStatus().name())
                    .leaderboard(currentLeaderboard)
                    .lastUpdated(currentLeaderboard.lastUpdated())
                    .build();
        } catch (Exception e) {
            log.error("Error converting session to historical response", e);
            return HistoricalLeaderboardResponse.builder()
                    .sessionId(session.getId())
                    .sessionName(session.getSessionName())
                    .sessionCode(session.getSessionCode())
                    .hostName(session.getHostName())
                    .startTime(session.getStartTime())
                    .endTime(session.getEndTime())
                    .totalParticipants(session.getTotalParticipants())
                    .totalQuestions(session.getTotalQuestions())
                    .status("ERROR")
                    .leaderboard(LeaderboardResponse.builder()
                            .sessionId(session.getId())
                            .entries(List.of())
                            .totalParticipants(0)
                            .lastUpdated(System.currentTimeMillis())
                            .status("ERROR")
                            .build())
                    .lastUpdated(System.currentTimeMillis())
                    .build();
        }
    }
}
