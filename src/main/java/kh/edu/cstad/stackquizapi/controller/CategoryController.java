package kh.edu.cstad.stackquizapi.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
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

    @Operation(summary = "Create category quizzes",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CategoryResponse createCategory(@Valid @RequestBody  CategoryRequest categoryRequest) {
        return categoryService.createCategory(categoryRequest);
    }

    @Operation(summary = "Create categories quizzes",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/batch")
    public List<CategoryResponse> createCategories(@Valid @RequestBody List<CategoryRequest> categoryRequests) {
        return categoryRequests.stream()
                .map(categoryService::createCategory)
                .toList();
    }

    @Operation(summary = "Get all categories",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<CategoryResponse> getAllCategories() {
        return categoryService.getAllCategories();
    }

}