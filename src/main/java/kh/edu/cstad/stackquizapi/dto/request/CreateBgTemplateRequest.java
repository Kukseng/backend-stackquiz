package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateBgTemplateRequest(

        @NotBlank(message = "Template Image is required")
        String templateImage

) {
}
