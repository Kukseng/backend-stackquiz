package kh.edu.cstad.stackquizapi.dto.response;

import kh.edu.cstad.stackquizapi.util.QuestionType;

import java.util.List;

public record QuestionResponse(

        String id,

        String text,

        QuestionType type,

        Integer questionOrder,

        Integer timeLimit,

        Integer points,

        String imageUrl,

        List<OptionResponse> options
) {
}
