package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository
        extends JpaRepository<User, String> {

    boolean existsByEmail(String email);

    Optional<User> findById(String Id);

    Optional<User> findByIdAndIsActiveFalse(String  userId);

//    Optional<User> findByIdAndIsActiveTrue(String userId);

    List<User> findAllByIsActiveFalse();

//    List<User> findUserByUserId(Integer userId);

    Optional<User> findByIdAndIsActiveTrue (String userId);
    List<User> findAllByIsActiveTrue ();


}