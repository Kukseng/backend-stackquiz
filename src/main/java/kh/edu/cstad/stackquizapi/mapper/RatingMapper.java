package kh.edu.cstad.stackquizapi.mapper;

import kh.edu.cstad.stackquizapi.domain.Rating;
import kh.edu.cstad.stackquizapi.dto.request.RatingRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RatingMapper {

    Rating fromRatingRequest(RatingRequest ratingRequest);

}
