package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.QuizFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizFeedbackRepository extends JpaRepository<QuizFeedback, String> {
}
