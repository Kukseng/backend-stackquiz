package kh.edu.cstad.stackquizapi.controller;

import kh.edu.cstad.stackquizapi.dto.request.ReportRequest;
import kh.edu.cstad.stackquizapi.dto.response.ReportResponse;
import kh.edu.cstad.stackquizapi.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/sessionId/{sessionId}")
    public ReportResponse createReport(
            @PathVariable String sessionId,
            @RequestBody ReportRequest request){
        return reportService.createReport(sessionId, request);
    }
}
