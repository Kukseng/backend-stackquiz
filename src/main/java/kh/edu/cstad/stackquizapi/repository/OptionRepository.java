package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.Option;
import org.springframework.data.jpa.repository.JpaRepository;

//import java.util.Optional;

public interface OptionRepository extends JpaRepository<Option, String> {
}
