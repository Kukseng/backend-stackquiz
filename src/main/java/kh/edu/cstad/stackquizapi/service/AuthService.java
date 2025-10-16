package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.*;
import kh.edu.cstad.stackquizapi.dto.response.LoginResponse;
import kh.edu.cstad.stackquizapi.dto.response.RegisterResponse;
import kh.edu.cstad.stackquizapi.dto.response.UserProfileResponse;
import org.springframework.http.ResponseEntity;

/**
 * Service interface for handling authentication-related operations.
 * Defines methods for registering new users and verifying their email addresses.
 *
 * @author PECH RATTANAKMONY
 */
public interface AuthService {

    /**
     * Registers a new user account.
     *
     * @param request the registration details such as username, password, and email
     * @return a response containing user details or registration status
     */
    RegisterResponse register(RegisterRequest request);

    /**
     * Verifies the email address of a registered user.
     *
     * @param userId the unique ID of the user to verify
     */
    void verifyEmail(String userId);

    /**
     * Initiates the password reset process by sending a reset link or token
     * to the provided email address.
     *
     * @param email the email address associated with the user account
     */
    void requestPasswordReset(String email);

    /**
     * Resets the password for a user using a valid reset token.
     *
     * @param request the reset request containing token and new password
     */
    void resetPassword(ResetPasswordRequest request);

    //
    ResponseEntity<RegisterResponse> oauthRegister(OAuthRegisterRequest request);

}