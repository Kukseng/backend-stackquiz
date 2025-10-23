package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.CategoryRequest;
import kh.edu.cstad.stackquizapi.dto.response.CategoryResponse;

import java.util.List;

/**
 * Service interface for managing quiz or content categories.
 * Provides methods to create and retrieve category information.
 *
 * @author Pech Rattanakmony
 * @since 1.0
 */
public interface CategoryService {

    /**
     * Creates a new category based on the provided request data.
     *
     * @param categoryRequest the data used to create a new category
     * @return the details of the newly created category
     */
    CategoryResponse createCategory(CategoryRequest categoryRequest);

    /**
     * Retrieves all available categories.
     *
     * @return a list of all category responses
     */
    List<CategoryResponse> getAllCategories();
}
