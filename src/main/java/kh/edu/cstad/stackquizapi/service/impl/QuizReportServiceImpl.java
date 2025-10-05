package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.Quiz;
import kh.edu.cstad.stackquizapi.domain.QuizReport;
import kh.edu.cstad.stackquizapi.domain.User;
import kh.edu.cstad.stackquizapi.dto.request.QuizReportRequest;
import kh.edu.cstad.stackquizapi.dto.response.QuizReportResponse;
import kh.edu.cstad.stackquizapi.mapper.QuizReportMapper;
import kh.edu.cstad.stackquizapi.repository.QuizReportRepository;
import kh.edu.cstad.stackquizapi.repository.QuizRepository;
import kh.edu.cstad.stackquizapi.repository.UserRepository;
import kh.edu.cstad.stackquizapi.service.QuizReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizReportServiceImpl implements QuizReportService {

    private final QuizReportRepository quizReportRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuizReportMapper quizReportMapper;

    @Override
    @Transactional
    public QuizReportResponse submitReport(String quizId, QuizReportRequest request, Jwt accessToken) {
        String userId = accessToken.getSubject();

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean alreadyReported = quizReportRepository.existsByQuizAndUser(quiz, user);
        if (alreadyReported) {
            throw new IllegalStateException("You have already reported this quiz.");
        }

        QuizReport report = quizReportMapper.fromQuizReportRequest(request);
        report.setCreatedAt(LocalDateTime.now());

        quizReportRepository.save(report);

        long reportCount = quizReportRepository.countByQuiz(quiz);
        long REPORT_THRESHOLD = 3;

        if (reportCount >= REPORT_THRESHOLD) {
            quiz.setFlagged(true);
            quizRepository.save(quiz);
        }

        return new QuizReportResponse(
                report.getId(),
                quiz.getId(),
                user.getId(),
                report.getReason(),
                report.getDescription(),
                report.getCreatedAt()
        );
    }

    @Override
    public List<QuizReportResponse> getCurrentUserReports(Jwt accessToken) {
        String userId = accessToken.getSubject();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return quizReportRepository.findByUser(user)
                .stream()
                .map(r -> new QuizReportResponse(
                        r.getId(),
                        r.getQuiz().getId(),
                        r.getUser().getId(),
                        r.getReason(),
                        r.getDescription(),
                        r.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<QuizReportResponse> getReportsByQuiz(String quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        return quizReportRepository.findByQuiz(quiz)
                .stream()
                .map(r -> new QuizReportResponse(
                        r.getId(),
                        r.getQuiz().getId(),
                        r.getUser().getId(),
                        r.getReason(),
                        r.getDescription(),
                        r.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

}
