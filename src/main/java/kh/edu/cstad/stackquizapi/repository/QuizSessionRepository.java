package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.util.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QuizSessionRepository extends JpaRepository<QuizSession, String> {

    Optional<QuizSession> findById(String id);
    // Find session by unique session code
    Optional<QuizSession> findBySessionCode(String sessionCode);

    // Find sessions by host, ordered by creation date (most recent first)
    List<QuizSession> findByHostIdOrderByCreatedAtDesc(String hostId);

    // Find sessions by host with pagination
    Page<QuizSession> findByHostIdOrderByCreatedAtDesc(String hostId, Pageable pageable);

    // Find sessions by specific status
    List<QuizSession> findByStatus(Status status);

    // Find sessions by multiple statuses
    List<QuizSession> findByStatusIn(List<Status> statuses);

    // Find sessions by status with pagination (corrected method)
    Page<QuizSession> findByStatusOrderByCreatedAtDesc(Status status, Pageable pageable);

    // Find sessions by host and status with pagination (corrected method)
    Page<QuizSession> findByHostIdAndStatusOrderByCreatedAtDesc(String hostId, Status status, Pageable pageable);

    // Find the most recent session by host and status (single result)
    Optional<QuizSession> findFirstByHostIdAndStatusOrderByCreatedAtDesc(String hostId, Status status);

    // Additional useful queries for leaderboard service

    // Find sessions within date range
    List<QuizSession> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    // Find sessions by host within date range
    List<QuizSession> findByHostIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            String hostId, LocalDateTime startDate, LocalDateTime endDate);

    // Find sessions with pagination and date range
    Page<QuizSession> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find ended sessions (for historical reports)
    Page<QuizSession> findByStatusAndEndTimeIsNotNullOrderByEndTimeDesc(Status status, Pageable pageable);

    // Find active sessions (WAITING or IN_PROGRESS)
    @Query("SELECT qs FROM QuizSession qs WHERE qs.status IN :statuses ORDER BY qs.createdAt DESC")
    List<QuizSession> findActiveSessionsByStatus(@Param("statuses") List<Status> statuses);


    long countByStatus(Status status);

    // Count sessions by host
    long countByHostId(String hostId);


    @Query("SELECT qs FROM QuizSession qs WHERE qs.endTime BETWEEN :startTime AND :endTime ORDER BY qs.endTime DESC")
    List<QuizSession> findSessionsEndedBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);


    boolean existsBySessionCode(String sessionCode);


    List<QuizSession> findByQuizIdOrderByCreatedAtDesc(String quizId);


    @Query("SELECT qs FROM QuizSession qs WHERE qs.host.id = :hostId ORDER BY qs.createdAt DESC")
    List<QuizSession> findRecentSessionsByHost(@Param("hostId") String hostId, Pageable pageable);
}