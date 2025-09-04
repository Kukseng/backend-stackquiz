package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.domain.Report;
import kh.edu.cstad.stackquizapi.domain.User;
import kh.edu.cstad.stackquizapi.dto.request.ReportRequest;
import kh.edu.cstad.stackquizapi.dto.response.ReportResponse;
import kh.edu.cstad.stackquizapi.repository.QuizSessionRepository;
import kh.edu.cstad.stackquizapi.repository.ReportRepository;
import kh.edu.cstad.stackquizapi.repository.UserRepository;
import kh.edu.cstad.stackquizapi.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final QuizSessionRepository quizSessionRepository;

    @Override
    public ReportResponse createReport(String sessionId, ReportRequest request) {

        // check if user Exists
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User Not Found"));

        // check sessionId
        QuizSession quizSession = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Session Not Found"));

        //Create and Save To Database
        Report report = new Report();
        report.setUser(user);
        report.setSession(quizSession);

        reportRepository.save(report);

        return new ReportResponse(report.getId(), user.getId(), quizSession.getId());
    }
}
