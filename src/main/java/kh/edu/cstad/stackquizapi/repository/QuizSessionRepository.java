package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.Question;
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

    @Query("SELECT qs FROM QuizSession qs " +
            "JOIN FETCH qs.quiz q " +
            "LEFT JOIN FETCH q.questions " +
            "WHERE qs.id = :sessionId")
    Optional<QuizSession> findByIdWithQuestions(@Param("sessionId") String sessionId);


    @Query("SELECT qs FROM QuizSession qs JOIN FETCH qs.quiz q LEFT JOIN FETCH q.questions WHERE qs.sessionCode = :code")
    Optional<QuizSession> findBySessionCodeWithQuestions(@Param("code") String code);





    List<QuizSession> findByStatusIn(List<Status> statuses);

    Page<QuizSession> findByStatusOrderByCreatedAtDesc(Status status, Pageable pageable);

    Page<QuizSession> findByHostIdAndStatusOrderByCreatedAtDesc(String hostId, Status status, Pageable pageable);



    @Query("SELECT qs FROM QuizSession qs WHERE qs.endTime BETWEEN :startTime AND :endTime ORDER BY qs.endTime DESC")
    List<QuizSession> findSessionsEndedBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);



    @Query("SELECT qs FROM QuizSession qs WHERE qs.host.id = :hostId ORDER BY qs.createdAt DESC")
    List<QuizSession> findRecentSessionsByHost(@Param("hostId") String hostId, Pageable pageable);

    // Admin Dashboard Methods
    Long countByStatus(Status status);
    Long countByCreatedAtAfter(LocalDateTime date);
    Long countByStatusAndCreatedAtAfter(Status status, LocalDateTime date);
    List<QuizSession> findByStatus(Status status);
    List<QuizSession> findByQuizId(String quizId);
    QuizSession findTopByOrderByCreatedAtDesc();
    QuizSession findTopByStatusOrderByEndTimeDesc(Status status);
}