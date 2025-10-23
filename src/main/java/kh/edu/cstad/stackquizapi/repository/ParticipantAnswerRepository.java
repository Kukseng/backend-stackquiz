package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.ParticipantAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipantAnswerRepository extends JpaRepository<ParticipantAnswer, String> {

    boolean existsByParticipantIdAndQuestionId (String participantId, String questionId);

    List<ParticipantAnswer> findByParticipantIdOrderByAnsweredAt(String participantId);

    Optional<ParticipantAnswer> findByParticipantIdAndQuestionId(String participantId, String questionId);

    List<ParticipantAnswer> findByParticipantIdOrderByAnsweredAtAsc(String participantId);

    long countByParticipantId(String participantId);

    Long countByIsCorrectTrue();

    Long countByAnsweredAtAfter(java.time.LocalDateTime date);

    Long countByParticipantIdIn(List<String> participantIds);

    Long countByParticipantIdInAndIsCorrectTrue(List<String> participantIds);

    @Query("SELECT pa FROM ParticipantAnswer pa " +
            "WHERE pa.participant.session.id = :sessionId")
    List<ParticipantAnswer> findByParticipantSessionId(@Param("sessionId") String sessionId);

    @Query("SELECT pa FROM ParticipantAnswer pa " +
            "WHERE pa.question.id = :questionId " +
            "AND pa.participant.session.id = :sessionId")
    List<ParticipantAnswer> findByQuestionIdAndParticipantSessionId(
            @Param("questionId") String questionId,
            @Param("sessionId") String sessionId
    );

    @Query("SELECT COUNT(pa) FROM ParticipantAnswer pa " +
            "JOIN pa.participant p " +
            "WHERE p.session.id = :sessionId")
    long countBySessionId(@Param("sessionId") String sessionId);

    @Query("SELECT COUNT(pa) FROM ParticipantAnswer pa " +
            "JOIN pa.participant p " +
            "WHERE p.session.id = :sessionId AND pa.isCorrect = :isCorrect")
    long countBySessionIdAndIsCorrect(@Param("sessionId") String sessionId, @Param("isCorrect") boolean isCorrect);

}