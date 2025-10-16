package kh.edu.cstad.stackquizapi.dto.response;

import lombok.Builder;

@Builder
public record BackgroundTemplateResponse(

        String id,

        String templateImage

) {
}
