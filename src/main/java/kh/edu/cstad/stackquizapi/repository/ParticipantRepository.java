package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, String> {




    boolean existsBySessionIdAndNickname (String sessionId, String nickname);

    List<Participant> findBySessionIdAndIsActiveTrue (String sessionId);

    List<Participant> findBySessionId (String sessionId);





    int countBySessionId(String sessionId);


    int countBySessionIdAndIsActiveTrue(String sessionId);

    List<Participant> findBySessionIdOrderByTotalScoreDesc (String sessionId);
}