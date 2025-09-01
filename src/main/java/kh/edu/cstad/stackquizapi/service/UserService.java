package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.CreateUserRequest;
import kh.edu.cstad.stackquizapi.dto.request.UpdateUserRequest;
import kh.edu.cstad.stackquizapi.dto.response.UserResponse;

import java.util.List;

/**
 * service interface users management
 *
 * @author Ben Loemheng
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
     * @param userId the unique identifier of the user
     * @return the user response DTO if found
     */
    UserResponse getUserById(String userId);

    /**
     * Updates the details of a user by their ID.
     *
     * @param userId the unique identifier of the user to update
     * @param updateUserRequest the request object containing updated user details
     * @return the updated user response DTO
     */
    UserResponse updateUserByUserId(String userId, UpdateUserRequest updateUserRequest);

    /**
     * Deletes a user by their ID.
     *
     * @param userId the unique identifier of the user to delete
     */
    void deleteUserByUserId(String userId);

    /**
     * Retrieves all users in the system.
     *
     * @return a list of user response DTOs
     */
    List<UserResponse> findAll();

}