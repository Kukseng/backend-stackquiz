package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.domain.Participant;

import kh.edu.cstad.stackquizapi.dto.request.JoinSessionRequest;
import kh.edu.cstad.stackquizapi.dto.request.SubmitAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.response.ParticipantResponse;
import kh.edu.cstad.stackquizapi.dto.response.SubmitAnswerResponse;

import java.util.List;
import java.util.Optional;

public interface ParticipantService {

    ParticipantResponse joinSession(JoinSessionRequest request);

    SubmitAnswerResponse submitAnswer(SubmitAnswerRequest request);

    List<ParticipantResponse> getSessionParticipants(String sessionId);

//    LeaderboardResponse getLeaderboard(String sessionId);

    void leaveSession(String participantId);

    Optional<Participant> getParticipantById(String participantId);


    boolean canJoinSession(String sessionCode);

    boolean isNicknameAvailable(String sessionId, String nickname);

}