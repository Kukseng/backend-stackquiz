package kh.edu.cstad.stackquizapi.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record FullSessionReportResponse(
        SessionReportSummaryResponse summary,
        List<ParticipantReportResponse> participants,
        List<QuestionReportResponse> questions
) {
}

