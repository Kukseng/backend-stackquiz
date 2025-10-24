package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.CreateUserRequest;
import kh.edu.cstad.stackquizapi.dto.request.UpdateUserRequest;
import kh.edu.cstad.stackquizapi.dto.response.UserResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * service interface users management
 *
 * @author Ben Loemheng
 * @since 1.0
 * */
public interface UserService{

    /**
     * Creates a new user.
     *
     * @param createUserRequest the request object containing user details
     * @return the created user's response DTO
     */
    UserResponse createUser(CreateUserRequest createUserRequest);

    /**
     * Retrieves a user by their unique ID.
     *
     * @return the user response DTO if found
     */
    UserResponse getCurrentUser(Jwt accessToken);

    /**
     * Updates the details of a user by their ID.
     *
     * @param updateUserRequest the request object containing updated user details
     * @return the updated user response DTO
     */
    UserResponse updateUser(Jwt accessToken, UpdateUserRequest updateUserRequest);

    UserResponse updateUserByAdmin(String id, UpdateUserRequest updateUserRequest);

    /**
     * Deletes a user by their ID.
     *
     * @param userId the unique identifier of the user to delete
     */
    void deleteUserByUserId(String userId);

    void disableUserByUserId(String userId);

    /**
     * Retrieves all users in the system.
     *
     * @return a list of user response DTOs
     */
    List<UserResponse> findAll();

}