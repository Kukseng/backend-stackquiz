package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.Quiz;
import kh.edu.cstad.stackquizapi.domain.QuizReport;
import kh.edu.cstad.stackquizapi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizReportRepository extends JpaRepository<QuizReport, String> {

    // Prevent duplicate reports
    boolean existsByQuizAndUser(Quiz quiz, User user);

    // Count how many reports a quiz has
    long countByQuiz(Quiz quiz);

    // Find all reports submitted by a user
    List<QuizReport> findByUser(User user);

    // Find all reports for a specific quiz
    List<QuizReport> findByQuiz(Quiz quiz);

}