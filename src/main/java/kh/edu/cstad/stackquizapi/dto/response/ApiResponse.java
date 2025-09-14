package kh.edu.cstad.stackquizapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ApiResponse<T> {

    private boolean success;

    private String message;

    private String errorCode;

    private int status;

    private T data;

    private Object meta;

    private String path;

    private Instant timestamp;

    private String traceId;

}
