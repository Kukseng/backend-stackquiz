package kh.edu.cstad.stackquizapi.mapper;

import javax.annotation.processing.Generated;
import kh.edu.cstad.stackquizapi.domain.Category;
import kh.edu.cstad.stackquizapi.dto.request.CategoryRequest;
import kh.edu.cstad.stackquizapi.dto.response.CategoryResponse;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-15T23:18:48+0700",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.8 (Microsoft)"
)
@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Override
    public CategoryResponse toCategoryResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        String id = null;
        String name = null;

        id = category.getId();
        name = category.getName();

        CategoryResponse categoryResponse = new CategoryResponse( id, name );

        return categoryResponse;
    }

    @Override
    public Category mapCategoryRequest(CategoryRequest categoryRequest) {
        if ( categoryRequest == null ) {
            return null;
        }

        Category category = new Category();

        category.setName( categoryRequest.name() );
        category.setDescription( categoryRequest.description() );

        return category;
    }
}
