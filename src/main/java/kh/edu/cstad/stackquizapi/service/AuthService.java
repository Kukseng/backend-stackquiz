package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.LoginRequest;
import kh.edu.cstad.stackquizapi.dto.request.LogoutRequest;
import kh.edu.cstad.stackquizapi.dto.request.RefreshTokenRequest;
import kh.edu.cstad.stackquizapi.dto.request.RegisterRequest;
import kh.edu.cstad.stackquizapi.dto.request.ResetPasswordRequest;
import kh.edu.cstad.stackquizapi.dto.response.LoginResponse;
import kh.edu.cstad.stackquizapi.dto.response.RegisterResponse;
import kh.edu.cstad.stackquizapi.dto.response.UserProfileResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

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

    void verifyEmail(String userId);











}
