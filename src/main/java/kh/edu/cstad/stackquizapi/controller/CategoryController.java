package kh.edu.cstad.stackquizapi.controller;


import kh.edu.cstad.stackquizapi.dto.request.CategoryRequest;
import kh.edu.cstad.stackquizapi.dto.response.CategoryResponse;
import kh.edu.cstad.stackquizapi.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;


    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CategoryResponse createCategory(@RequestBody  CategoryRequest categoryRequest) {
        return categoryService.createCategory(categoryRequest);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/batch")
    public List<CategoryResponse> createCategories(@RequestBody List<CategoryRequest> categoryRequests) {
        return categoryRequests.stream()
                .map(categoryService::createCategory)
                .toList();
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<CategoryResponse> getAllCategories() {
        return categoryService.getAllCategories();
    }

}