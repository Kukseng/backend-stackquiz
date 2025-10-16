package kh.edu.cstad.stackquizapi.controller;

import jakarta.validation.Valid;
import kh.edu.cstad.stackquizapi.dto.request.BulkAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.request.SubmitAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.response.ParticipantAnswerResponse;
import kh.edu.cstad.stackquizapi.service.ParticipantAnswerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/answers")
@RequiredArgsConstructor
public class ParticipantAnswerController {

    private final ParticipantAnswerService participantAnswerService;

    @PostMapping("/submit")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipantAnswerResponse submitAnswer(
            @Valid @RequestBody SubmitAnswerRequest request) {


        return participantAnswerService.submitAnswer(request);
    }

    @PostMapping("/submit/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ParticipantAnswerResponse> submitBulkAnswers(
            @Valid @RequestBody BulkAnswerRequest request) {


        return participantAnswerService.submitBulkAnswers(request);
    }

    @GetMapping("/participant/{participantId}")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipantAnswerResponse> getParticipantAnswers(
            @PathVariable String participantId) {

        return participantAnswerService.getParticipantAnswers(participantId);
    }

    @GetMapping("/participant/{participantId}/question/{questionId}")
    @ResponseStatus(HttpStatus.OK)
    public ParticipantAnswerResponse getParticipantQuestionAnswer(
            @PathVariable String participantId,
            @PathVariable String questionId) {

        return participantAnswerService.getParticipantQuestionAnswer(participantId, questionId);
    }

    @PutMapping("/{answerId}")
    @ResponseStatus(HttpStatus.OK)
    public ParticipantAnswerResponse updateAnswer(
            @PathVariable String answerId,
            @Valid @RequestBody SubmitAnswerRequest request) {

        return participantAnswerService.updateAnswer(answerId, request);
    }


}
