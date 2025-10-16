package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, String> {

    boolean existsBySessionIdAndNickname (String sessionId, String nickname);

    List<Participant> findBySessionIdAndIsActiveTrue (String sessionId);

    List<Participant> findBySessionId (String sessionId);

    int countBySessionId(String sessionId);

    Optional<Participant> findByIdAndIsActiveTrue (String id);

    int countBySessionIdAndIsActiveTrue(String sessionId);

    List<Participant> findBySessionIdOrderByTotalScoreDesc (String sessionId);

    @Query("SELECT COUNT(p) + 1 FROM Participant p WHERE p.session.id = :sessionId " +
            "AND p.totalScore > :score AND p.isActive = true")
    int getParticipantPosition(@Param("sessionId") String sessionId, @Param("score") Integer score);


    @Query("SELECT p FROM Participant p WHERE p.session.id = :sessionId AND p.isActive = true ORDER BY p.totalScore DESC")
    List<Participant> findBySessionIdAndIsActiveTrueOrderByTotalScoreDesc(@Param("sessionId") String sessionId);
}