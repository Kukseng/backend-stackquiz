package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.FeedbackSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedbackSummaryRepository extends JpaRepository<FeedbackSummary, String> {
    Optional<FeedbackSummary> findBySession_Id(String sessionId);
}
