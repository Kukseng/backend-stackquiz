package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.CategoryRequest;
import kh.edu.cstad.stackquizapi.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest categoryRequest);

    List<CategoryResponse> getAllCategories();
}
