package kh.edu.cstad.stackquizapi.dto.response;

import lombok.Builder;

@Builder
public record QuestionReportResponse(

        String questionId,

        String questionText,

        Integer questionNumber,

        Double accuracy,

        Integer correctAnswers,

        Integer incorrectAnswers,

        Integer unattemptedAnswers

) {
}

