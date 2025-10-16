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
import kh.edu.cstad.stackquizapi.util.QuizStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    @Value("${app.report.threshold}")
    private long REPORT_THRESHOLD;

    @Override
    @Transactional
    public QuizReportResponse submitReport(String quizId, QuizReportRequest request, Jwt accessToken) {
        String userId = accessToken.getSubject();
        log.info("User ID form jwt token: {}", userId);

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Quiz not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found"));

        boolean alreadyReported = quizReportRepository.existsByQuizAndUser(quiz, user);
        if (alreadyReported) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You have already reported this quiz.");
        }

        QuizReport report = quizReportMapper.fromQuizReportRequest(request);
        report.setQuiz(quiz);
        report.setUser(user);
        report.setCreatedAt(LocalDateTime.now());
        quizReportRepository.save(report);

        long reportCount = quizReportRepository.countByQuiz(quiz);

        if (reportCount >= REPORT_THRESHOLD) {
            quiz.setFlagged(true);

            if (!quiz.getStatus().equals(QuizStatus.SUSPENDED)) {
                quiz.setStatus(QuizStatus.SUSPENDED);
                quiz.setIsActive(false);
            }

            quizRepository.save(quiz);
            log.info("Quiz {} has been auto-suspended after reaching {} reports", quiz.getTitle(), reportCount);
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
