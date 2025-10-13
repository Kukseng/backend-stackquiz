package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, String> {

    Boolean existsByName(String name);

}