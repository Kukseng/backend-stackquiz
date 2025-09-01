package kh.edu.cstad.stackquizapi.controller;

import jakarta.validation.Valid;
import kh.edu.cstad.stackquizapi.dto.request.LoginRequest;
import kh.edu.cstad.stackquizapi.dto.request.LogoutRequest;
import kh.edu.cstad.stackquizapi.dto.request.RefreshTokenRequest;
import kh.edu.cstad.stackquizapi.dto.request.RegisterRequest;
import kh.edu.cstad.stackquizapi.dto.request.ResetPasswordRequest;
import kh.edu.cstad.stackquizapi.dto.response.LoginResponse;
import kh.edu.cstad.stackquizapi.dto.response.RegisterResponse;
import kh.edu.cstad.stackquizapi.dto.response.UserProfileResponse;
import kh.edu.cstad.stackquizapi.exception.ApiResponse;
import kh.edu.cstad.stackquizapi.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Registration request received for username: {}", request.username());

        RegisterResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<RegisterResponse>builder()
                        .success(true)
                        .message("User registered successfully")
                        .data(response)
                        .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login request received for username: {}", request.username());

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.<LoginResponse>builder()
                .success(true)
                .message("Login successful")
                .data(response)
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        log.info("Token refresh request received");

        LoginResponse response = authService.refreshToken(request);

        return ResponseEntity.ok(ApiResponse.<LoginResponse>builder()
                .success(true)
                .message("Token refreshed successfully")
                .data(response)
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request) {

        log.info("Logout request received for user: {}", request.userId());

        authService.logout(request);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Logout successful")
                .build());
    }

    @PostMapping("/verify-email/{userId}")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@PathVariable String userId) {

        log.info("Email verification request for user: {}", userId);

        authService.verifyEmail(userId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Verification email sent successfully")
                .build());
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
            @RequestParam String email) {

        log.info("Password reset request for email: {}", email);

        authService.requestPasswordReset(email);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Password reset email sent successfully")
                .build());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        log.info("Password reset for email: {}", request.email());

        authService.resetPassword(request);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Password reset successful")
                .build());
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
            @PathVariable String userId) {

        log.info("Profile request for user: {}", userId);

        UserProfileResponse response = authService.getUserProfile(userId);

        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>builder()
                .success(true)
                .message("User profile retrieved successfully")
                .data(response)
                .build());
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Auth service is running")
                .data("OK")
                .build());
    }
}
