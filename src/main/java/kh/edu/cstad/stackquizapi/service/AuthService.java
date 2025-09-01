package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.LoginRequest;
import kh.edu.cstad.stackquizapi.dto.request.LogoutRequest;
import kh.edu.cstad.stackquizapi.dto.request.RefreshTokenRequest;
import kh.edu.cstad.stackquizapi.dto.request.RegisterRequest;
import kh.edu.cstad.stackquizapi.dto.request.ResetPasswordRequest;
import kh.edu.cstad.stackquizapi.dto.response.LoginResponse;
import kh.edu.cstad.stackquizapi.dto.response.RegisterResponse;
import kh.edu.cstad.stackquizapi.dto.response.UserProfileResponse;

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
     * Authenticates a user with their login credentials.
     *
     * @param request the login request containing username/email and password
     * @return a response with authentication tokens and user session details
     */
    LoginResponse login(LoginRequest request);

    /**
     * Refreshes the authentication token using a valid refresh token.
     *
     * @param request the refresh token request containing the current refresh token
     * @return a new set of authentication tokens
     */
    LoginResponse refreshToken(RefreshTokenRequest request);

    /**
     * Logs the user out by invalidating their session and tokens.
     *
     * @param request the logout request containing user/session information
     */
    void logout(LogoutRequest request);

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

    /**
     * Retrieves the profile details of a user.
     *
     * @param userId the unique ID of the user
     * @return the profile information of the user
     */
    UserProfileResponse getUserProfile(String userId);
}
