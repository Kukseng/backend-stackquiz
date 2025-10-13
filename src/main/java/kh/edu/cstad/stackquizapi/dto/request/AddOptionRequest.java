package kh.edu.cstad.stackquizapi.dto.request;

public record AddOptionRequest(

        String optionText,

        Boolean isCorrected
){
}
