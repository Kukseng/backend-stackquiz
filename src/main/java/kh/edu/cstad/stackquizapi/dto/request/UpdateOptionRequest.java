package kh.edu.cstad.stackquizapi.dto.request;

public record UpdateOptionRequest(

        String optionText,

        Integer optionOrder,

        Boolean isCorrected

) {
}
