package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.QuizCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizCategoryRepository extends JpaRepository<QuizCategory, String> { }
