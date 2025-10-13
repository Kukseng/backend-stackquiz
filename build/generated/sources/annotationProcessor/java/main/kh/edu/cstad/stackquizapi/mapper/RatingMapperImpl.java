package kh.edu.cstad.stackquizapi.mapper;

import javax.annotation.processing.Generated;
import kh.edu.cstad.stackquizapi.domain.Rating;
import kh.edu.cstad.stackquizapi.dto.request.RatingRequest;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-12T22:04:26+0700",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.8 (Microsoft)"
)
@Component
public class RatingMapperImpl implements RatingMapper {

    @Override
    public Rating fromRatingRequest(RatingRequest ratingRequest) {
        if ( ratingRequest == null ) {
            return null;
        }

        Rating rating = new Rating();

        if ( ratingRequest.stars() != null ) {
            rating.setStars( ratingRequest.stars() );
        }
        rating.setComment( ratingRequest.comment() );

        return rating;
    }
}
