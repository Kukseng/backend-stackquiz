package kh.edu.cstad.stackquizapi.dto.response;

import java.sql.Timestamp;

public record OptionResponse(

        String id,

        String optionText,

        Integer optionOrder,

        Timestamp createdAt,

        Boolean isCorrected

) {
}
