package kh.edu.cstad.stackquizapi.exception;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse<T>(

        String message,

        Integer code,

        LocalDateTime timestamp,

        T details

) {
}
