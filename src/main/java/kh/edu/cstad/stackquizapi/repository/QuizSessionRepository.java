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

    Optional<QuizSession> findBySessionCode(String sessionCode);

    List<QuizSession> findByHostIdOrderByCreatedAtDesc(String hostId);

    Page<QuizSession> findByHostIdOrderByCreatedAtDesc(String hostId, Pageable pageable);

    List<QuizSession> findByStatus(Status status);

    List<QuizSession> findByStatusIn(List<Status> statuses);

    Page<QuizSession> findByStatusOrderByCreatedAtDesc(Status status, Pageable pageable);

    Page<QuizSession> findByHostIdAndStatusOrderByCreatedAtDesc(String hostId, Status status, Pageable pageable);

    Optional<QuizSession> findFirstByHostIdAndStatusOrderByCreatedAtDesc(String hostId, Status status);

    List<QuizSession> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    List<QuizSession> findByHostIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            String hostId, LocalDateTime startDate, LocalDateTime endDate);

    Page<QuizSession> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<QuizSession> findByStatusAndEndTimeIsNotNullOrderByEndTimeDesc(Status status, Pageable pageable);

    @Query("SELECT qs FROM QuizSession qs WHERE qs.status IN :statuses ORDER BY qs.createdAt DESC")
    List<QuizSession> findActiveSessionsByStatus(@Param("statuses") List<Status> statuses);

    long countByStatus(Status status);

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