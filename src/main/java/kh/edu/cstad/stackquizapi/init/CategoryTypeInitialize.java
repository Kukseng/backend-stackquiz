package kh.edu.cstad.stackquizapi.init;

import jakarta.annotation.PostConstruct;
import kh.edu.cstad.stackquizapi.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryTypeInitialize {

    private final CategoryRepository categoryRepository;

    @PostConstruct
    public void init(){

    }

}
