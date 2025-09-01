package kh.edu.cstad.stackquizapi.controller;

import jakarta.validation.Valid;
import kh.edu.cstad.stackquizapi.dto.request.BulkAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.request.SubmitAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.response.AnswerSummaryResponse;
import kh.edu.cstad.stackquizapi.dto.response.ParticipantAnswerResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuestionStatisticsResponse;
import kh.edu.cstad.stackquizapi.service.ParticipantAnswerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/answers")
@RequiredArgsConstructor
public class ParticipantAnswerController {

    private final ParticipantAnswerService participantAnswerService;

    @PostMapping("/submit")
    public ResponseEntity<ParticipantAnswerResponse> submitAnswer(
            @Valid @RequestBody SubmitAnswerRequest request) {

        log.info("Submitting answer for participant {} on question {}",
                request.participantId(), request.questionId());

        ParticipantAnswerResponse response = participantAnswerService.submitAnswer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/submit/bulk")
    public ResponseEntity<List<ParticipantAnswerResponse>> submitBulkAnswers(
            @Valid @RequestBody BulkAnswerRequest request) {

        log.info("Submitting bulk answers for participant {} in session {}",
                request.participantId(), request.sessionId());

        List<ParticipantAnswerResponse> responses = participantAnswerService.submitBulkAnswers(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @GetMapping("/participant/{participantId}")
    public ResponseEntity<List<ParticipantAnswerResponse>> getParticipantAnswers(
            @PathVariable String participantId) {

        List<ParticipantAnswerResponse> answers = participantAnswerService
                .getParticipantAnswers(participantId);
        return ResponseEntity.ok(answers);
    }

    @GetMapping("/participant/{participantId}/session/{sessionId}")
    public ResponseEntity<List<ParticipantAnswerResponse>> getParticipantSessionAnswers(
            @PathVariable String participantId,
            @PathVariable String sessionId) {

        List<ParticipantAnswerResponse> answers = participantAnswerService
                .getParticipantSessionAnswers(participantId, sessionId);
        return ResponseEntity.ok(answers);
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<ParticipantAnswerResponse>> getQuestionAnswers(
            @PathVariable String questionId) {

        List<ParticipantAnswerResponse> answers = participantAnswerService
                .getQuestionAnswers(questionId);
        return ResponseEntity.ok(answers);
    }

    @GetMapping("/question/{questionId}/session/{sessionId}")
    public ResponseEntity<List<ParticipantAnswerResponse>> getQuestionSessionAnswers(
            @PathVariable String questionId,
            @PathVariable String sessionId) {

        List<ParticipantAnswerResponse> answers = participantAnswerService
                .getQuestionSessionAnswers(questionId, sessionId);
        return ResponseEntity.ok(answers);
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<ParticipantAnswerResponse>> getSessionAnswers(
            @PathVariable String sessionId) {

        List<ParticipantAnswerResponse> answers = participantAnswerService
                .getSessionAnswers(sessionId);
        return ResponseEntity.ok(answers);
    }

    @GetMapping("/participant/{participantId}/summary")
    public ResponseEntity<AnswerSummaryResponse> getParticipantAnswerSummary(
            @PathVariable String participantId) {

        AnswerSummaryResponse summary = participantAnswerService
                .getParticipantAnswerSummary(participantId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/question/{questionId}/session/{sessionId}/statistics")
    public ResponseEntity<QuestionStatisticsResponse> getQuestionStatistics(
            @PathVariable String questionId,
            @PathVariable String sessionId) {

        QuestionStatisticsResponse statistics = participantAnswerService
                .getQuestionStatistics(questionId, sessionId);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/participant/{participantId}/question/{questionId}")
    public ResponseEntity<ParticipantAnswerResponse> getParticipantQuestionAnswer(
            @PathVariable String participantId,
            @PathVariable String questionId) {

        ParticipantAnswerResponse answer = participantAnswerService
                .getParticipantQuestionAnswer(participantId, questionId);
        return ResponseEntity.ok(answer);
    }

    @PostMapping("/validate")
    public ResponseEntity<ParticipantAnswerResponse> validateAndScoreAnswer(
            @Valid @RequestBody SubmitAnswerRequest request) {

        log.info("Validating answer for participant {} on question {}",
                request.participantId(), request.questionId());

        ParticipantAnswerResponse response = participantAnswerService
                .validateAndScoreAnswer(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{answerId}")
    public ResponseEntity<ParticipantAnswerResponse> updateAnswer(
            @PathVariable String answerId,
            @Valid @RequestBody SubmitAnswerRequest request) {

        log.info("Updating answer {}", answerId);

        ParticipantAnswerResponse response = participantAnswerService
                .updateAnswer(answerId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{answerId}")
    public ResponseEntity<Map<String, String>> deleteAnswer(
            @PathVariable String answerId) {

        log.info("Deleting answer {}", answerId);

        participantAnswerService.deleteAnswer(answerId);
        return ResponseEntity.ok(Map.of("message", "Answer deleted successfully"));
    }

    @GetMapping("/participant/{participantId}/question/{questionId}/exists")
    public ResponseEntity<Map<String, Boolean>> checkAnswerExists(
            @PathVariable String participantId,
            @PathVariable String questionId) {

        boolean hasAnswered = participantAnswerService.hasAnswered(participantId, questionId);
        return ResponseEntity.ok(Map.of("hasAnswered", hasAnswered));
    }

    @GetMapping("/participant/{participantId}/total-score")
    public ResponseEntity<Map<String, Integer>> getParticipantTotalScore(
            @PathVariable String participantId) {

        int totalScore = participantAnswerService.calculateParticipantTotalScore(participantId);
        return ResponseEntity.ok(Map.of("totalScore", totalScore));
    }
}