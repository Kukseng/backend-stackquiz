package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record RatingRequest(

        @NotNull(message = "Stars are required")
        @Min(value = 1, message = "Minimum rating is 1 star")
        @Max(value = 5, message = "Maximum rating is 5 stars")
        Integer stars,

        String comment

) {
}
