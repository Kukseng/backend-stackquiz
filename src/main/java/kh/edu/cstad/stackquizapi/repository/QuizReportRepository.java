package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.Quiz;
import kh.edu.cstad.stackquizapi.domain.QuizReport;
import kh.edu.cstad.stackquizapi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizReportRepository extends JpaRepository<QuizReport, String> {

    boolean existsByQuizAndUser(Quiz quiz, User user);

    long countByQuiz(Quiz quiz);

    List<QuizReport> findByUser(User user);

    List<QuizReport> findByQuiz(Quiz quiz);

}