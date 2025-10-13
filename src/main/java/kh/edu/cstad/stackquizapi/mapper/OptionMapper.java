package kh.edu.cstad.stackquizapi.mapper;

import kh.edu.cstad.stackquizapi.domain.Option;
import kh.edu.cstad.stackquizapi.dto.request.AddOptionRequest;
import kh.edu.cstad.stackquizapi.dto.request.UpdateOptionRequest;
import kh.edu.cstad.stackquizapi.dto.response.OptionResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface OptionMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "question", ignore = true)
    @Mapping(target = "participantAnswers", ignore = true)
    Option fromAddOptionRequest(AddOptionRequest addOptionRequest);

    OptionResponse toOptionResponse(Option option);

    @Mapping(target = "question", ignore = true)
    @Mapping(target = "participantAnswers", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void toQuestionPartially(
            UpdateOptionRequest updateOptionRequest,
            @MappingTarget Option option
    );
}
