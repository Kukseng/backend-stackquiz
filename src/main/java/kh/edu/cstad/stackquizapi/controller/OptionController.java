package kh.edu.cstad.stackquizapi.controller;

import kh.edu.cstad.stackquizapi.domain.Option;
import kh.edu.cstad.stackquizapi.dto.request.AddOptionRequest;
import kh.edu.cstad.stackquizapi.dto.request.UpdateOptionRequest;
import kh.edu.cstad.stackquizapi.dto.response.OptionResponse;
import kh.edu.cstad.stackquizapi.service.OptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/options")
@RequiredArgsConstructor
public class OptionController {

    private final OptionService optionService;

    @PostMapping
    public ResponseEntity<List<OptionResponse>> addNewOptions(
            @RequestParam(required = false) String questionId,
            @RequestBody List<AddOptionRequest> addOptionRequests) {

        List<OptionResponse> responses = optionService.addNewOptions(questionId, addOptionRequests);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @PutMapping("/{optionId}")
    public ResponseEntity<OptionResponse> updateOption(
            @PathVariable String optionId,
            @RequestBody UpdateOptionRequest updateOptionRequest) {

        OptionResponse response = optionService.updateOptionById(optionId, updateOptionRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{optionId}")
    public ResponseEntity<Void> deleteOption(
            @PathVariable String optionId,
            @RequestBody Option option) {

        optionService.deletedOptionById(optionId, option);
        return ResponseEntity.noContent().build();
    }
}
