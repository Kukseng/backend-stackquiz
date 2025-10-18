package kh.edu.cstad.stackquizapi.dto.request;

import kh.edu.cstad.stackquizapi.util.SatisfactionLevel;

public record CreateFeedbackRequest(

        String text,

        SatisfactionLevel satisfactionLevel

) {
}
