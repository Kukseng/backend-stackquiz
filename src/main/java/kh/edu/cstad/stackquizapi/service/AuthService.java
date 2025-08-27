package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.LoginRequest;
import kh.edu.cstad.stackquizapi.dto.request.RegisterRequest;
import kh.edu.cstad.stackquizapi.dto.response.RegisterResponse;

/**
 * Service interface for handling authentication-related operations.
 * Defines methods for registering new users and verifying their email addresses.
 *
 * @author PECH RATTANAKMONY
 */
public interface AuthService {

    /**
     * Registers a new user in the system.
     *
     * @param registerRequest the registration request containing user details
     *                        such as name, email, and password
     */
    RegisterResponse register(RegisterRequest registerRequest);

    void login(LoginRequest loginRequest);

    /**
     * Verifies the email address of a user.
     *
     * @param userId the unique identifier of the user whose email is to be verified
     */
    void verifyEmail(String userId);

    


}
