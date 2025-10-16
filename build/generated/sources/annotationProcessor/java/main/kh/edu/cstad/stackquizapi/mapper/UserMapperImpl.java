package kh.edu.cstad.stackquizapi.mapper;

import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import kh.edu.cstad.stackquizapi.domain.User;
import kh.edu.cstad.stackquizapi.dto.request.CreateUserRequest;
import kh.edu.cstad.stackquizapi.dto.request.UpdateUserRequest;
import kh.edu.cstad.stackquizapi.dto.response.UserResponse;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-16T16:34:38+0700",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.8 (Microsoft)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toUserResponse(User user) {
        if ( user == null ) {
            return null;
        }

        String id = null;
        String profileUser = null;
        String username = null;
        String email = null;
        String firstName = null;
        String lastName = null;
        String avatarUrl = null;
        boolean isActive = false;
        LocalDateTime createdAt = null;

        id = user.getId();
        profileUser = user.getProfileUser();
        username = user.getUsername();
        email = user.getEmail();
        firstName = user.getFirstName();
        lastName = user.getLastName();
        avatarUrl = user.getAvatarUrl();
        if ( user.getIsActive() != null ) {
            isActive = user.getIsActive();
        }
        createdAt = user.getCreatedAt();

        UserResponse userResponse = new UserResponse( id, profileUser, username, email, firstName, lastName, avatarUrl, isActive, createdAt );

        return userResponse;
    }

    @Override
    public void toCustomerPartially(UpdateUserRequest updateCustomerRequest, User user) {
        if ( updateCustomerRequest == null ) {
            return;
        }

        if ( updateCustomerRequest.username() != null ) {
            user.setUsername( updateCustomerRequest.username() );
        }
        if ( updateCustomerRequest.email() != null ) {
            user.setEmail( updateCustomerRequest.email() );
        }
        if ( updateCustomerRequest.avatarUrl() != null ) {
            user.setAvatarUrl( updateCustomerRequest.avatarUrl() );
        }
        if ( updateCustomerRequest.firstName() != null ) {
            user.setFirstName( updateCustomerRequest.firstName() );
        }
        if ( updateCustomerRequest.lastName() != null ) {
            user.setLastName( updateCustomerRequest.lastName() );
        }
    }

    @Override
    public User fromCreateUserRequest(CreateUserRequest dto) {
        if ( dto == null ) {
            return null;
        }

        User user = new User();

        user.setId( dto.id() );
        user.setUsername( dto.username() );
        user.setEmail( dto.email() );
        user.setAvatarUrl( dto.avatarUrl() );
        user.setFirstName( dto.firstName() );
        user.setLastName( dto.lastName() );
        user.setIsDeleted( dto.isDeleted() );

        return user;
    }
}
