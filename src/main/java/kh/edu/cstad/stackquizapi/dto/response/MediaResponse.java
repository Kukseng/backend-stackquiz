package kh.edu.cstad.stackquizapi.dto.response;

import lombok.Builder;

@Builder
public record MediaResponse(

        String name,

        String extension,

        String mimeTypeFile,

        String uri,

        Long size

) {
}
