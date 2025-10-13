package kh.edu.cstad.stackquizapi.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class DTOsException {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException e) {
        ErrorResponse <?> errorResponse = ErrorResponse
                .builder()
                .message(e.getBindingResult().getAllErrors().get(0).getDefaultMessage())
                .code(e.getStatusCode().value())
                .timestamp(LocalDateTime.now())
                .details(e.getMessage())
                .build();

        return ResponseEntity.status(e.getStatusCode().value()).body(errorResponse);
    }

}
