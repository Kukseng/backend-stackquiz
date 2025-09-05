package kh.edu.cstad.stackquizapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kh.edu.cstad.stackquizapi.domain.Option;
import kh.edu.cstad.stackquizapi.dto.request.AddOptionRequest;
import kh.edu.cstad.stackquizapi.dto.request.UpdateOptionRequest;
import kh.edu.cstad.stackquizapi.dto.response.OptionResponse;
import kh.edu.cstad.stackquizapi.service.OptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/options")
@RequiredArgsConstructor
public class OptionController {

    private final OptionService optionService;

    // Host: Add new options to a question
    @Operation(summary = "Add options to a question (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @PostMapping("/questions/{questionId}")
    public ResponseEntity<List<OptionResponse>> addNewOptions(
            @PathVariable String questionId,
            @RequestBody List<AddOptionRequest> addOptionRequests) {

        List<OptionResponse> responses = optionService.addNewOptions(questionId, addOptionRequests);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    // Host: Get all options (secured, admin use only)
    @Operation(summary = "Get all options (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @GetMapping
    public ResponseEntity<List<OptionResponse>> getAllOptions() {
        return ResponseEntity.ok(optionService.gelAllOptions());
    }

    //  Public: Players fetch options for a question
    @Operation(summary = "Get options by questionId (public)")
    @GetMapping("/questions/{questionId}/public")
    public ResponseEntity<List<OptionResponse>> getOptionsByQuestionId(@PathVariable String questionId) {
        return ResponseEntity.ok(optionService.getOptionsByQuestionId(questionId));
    }

    // Host: Update option
    @Operation(summary = "Update an option (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @PutMapping("/{optionId}")
    public ResponseEntity<OptionResponse> updateOption(
            @PathVariable String optionId,
            @RequestBody UpdateOptionRequest updateOptionRequest) {

        OptionResponse response = optionService.updateOptionById(optionId, updateOptionRequest);
        return ResponseEntity.ok(response);
    }

    // Host: Delete option
    @Operation(summary = "Delete an option (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @DeleteMapping("/{optionId}")
    public ResponseEntity<Void> deleteOption(@PathVariable String optionId) {
        optionService.deleteOptionById(optionId);
        return ResponseEntity.noContent().build();
    }
}
