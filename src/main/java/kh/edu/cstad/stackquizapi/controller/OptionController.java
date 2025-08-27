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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/options")
@RequiredArgsConstructor
public class OptionController {

    private final OptionService optionService;

    @PostMapping("/{questionId}")
    public ResponseEntity<OptionResponse> addNewOption(
            @PathVariable String questionId,
            @RequestBody AddOptionRequest addOptionRequest) {

        OptionResponse response = optionService.addNewOption(questionId, addOptionRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
