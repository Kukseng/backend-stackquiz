package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    boolean existsByEmail(String email);

    List<User> findAllByIsDeletedFalse();

    Optional<User> findByIdAndIsDeletedFalse(String id);

    boolean existsByUsernameAndIsDeletedTrue(String username);

}