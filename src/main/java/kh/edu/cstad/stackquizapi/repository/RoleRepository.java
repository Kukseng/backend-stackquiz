package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {
}
