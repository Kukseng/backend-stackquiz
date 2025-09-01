package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.domain.SessionReport;
import kh.edu.cstad.stackquizapi.dto.response.SessionReportResponse;

import java.util.List;

public interface SessionReportService {

    SessionReport generateReport(String sessionId);

    SessionReportResponse getReport(String sessionId);

    List<SessionReportResponse> getHostReports(String hostId);

}