package kh.edu.cstad.stackquizapi.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record ApiResponse<T>(

        boolean success,

        String message,

        T data,

        String error,

        Long timestamp

) {
}
