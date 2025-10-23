package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.util.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QuizSessionRepository extends JpaRepository<QuizSession, String> {

    Optional<QuizSession> findById(String id);

    Optional<QuizSession> findBySessionCode(String sessionCode);

    List<QuizSession> findByHostIdOrderByCreatedAtDesc(String hostId);

    List<QuizSession> findByStatusIn(List<Status> statuses);

    Page<QuizSession> findByStatusOrderByCreatedAtDesc(Status status, Pageable pageable);

    Page<QuizSession> findByHostIdAndStatusOrderByCreatedAtDesc(String hostId, Status status, Pageable pageable);

    // Admin Dashboard Methods
    Long countByStatus(Status status);

    Long countByCreatedAtAfter(LocalDateTime date);

    Long countByStatusAndCreatedAtAfter(Status status, LocalDateTime date);

    List<QuizSession> findByStatus(Status status);

    List<QuizSession> findByQuizId(String quizId);

    QuizSession findTopByOrderByCreatedAtDesc();

    QuizSession findTopByStatusOrderByEndTimeDesc(Status status);

}