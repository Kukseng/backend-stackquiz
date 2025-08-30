package kh.edu.cstad.stackquizapi.dto.request;

public record AddOptionRequest(

        String optionText,

        Integer optionOrder,

        Boolean isCorrected

){
}
