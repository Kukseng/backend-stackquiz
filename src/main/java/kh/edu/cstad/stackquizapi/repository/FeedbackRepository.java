package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.Feedback;
import kh.edu.cstad.stackquizapi.domain.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, String> {

    // find all comments for a single session
    List<Feedback> findBySession(QuizSession session);

}
