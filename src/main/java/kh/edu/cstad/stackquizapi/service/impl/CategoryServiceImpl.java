package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.Category;
import kh.edu.cstad.stackquizapi.dto.request.CategoryRequest;
import kh.edu.cstad.stackquizapi.dto.response.CategoryResponse;
import kh.edu.cstad.stackquizapi.mapper.CategoryMapper;
import kh.edu.cstad.stackquizapi.repository.CategoryRepository;
import kh.edu.cstad.stackquizapi.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;


    @Override
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        if (categoryRequest.name() == null || categoryRequest.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name is required");
        }

        if (categoryRepository.existsByName(categoryRequest.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists");
        }

        Category category = new Category();
        category.setName(categoryRequest.name());
        category.setDescription(categoryRequest.description());
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        Category saved = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(saved);
    }



    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
    }
}
