package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.ReportRequest;
import kh.edu.cstad.stackquizapi.dto.response.ReportResponse;

public interface ReportService {
    ReportResponse createReport(String sessionId, ReportRequest request);
}
