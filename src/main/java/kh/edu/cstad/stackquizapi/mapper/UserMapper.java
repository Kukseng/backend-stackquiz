package kh.edu.cstad.stackquizapi.mapper;

import kh.edu.cstad.stackquizapi.domain.User;
import kh.edu.cstad.stackquizapi.dto.request.CreateUserRequest;
import kh.edu.cstad.stackquizapi.dto.request.UpdateUserRequest;
import kh.edu.cstad.stackquizapi.dto.response.UserResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toUserResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void toCustomerPartially(
            UpdateUserRequest updateCustomerRequest,
            @MappingTarget User user
    );

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User fromCreateUserRequest(CreateUserRequest dto);

}