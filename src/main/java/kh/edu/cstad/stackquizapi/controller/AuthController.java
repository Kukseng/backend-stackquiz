package kh.edu.cstad.stackquizapi.controller;

import jakarta.validation.Valid;
import kh.edu.cstad.stackquizapi.dto.request.LoginRequest;

import kh.edu.cstad.stackquizapi.dto.request.OAuthRegisterRequest;
import kh.edu.cstad.stackquizapi.dto.request.RegisterRequest;
import kh.edu.cstad.stackquizapi.dto.response.LoginResponse;
import kh.edu.cstad.stackquizapi.dto.response.RegisterResponse;
import kh.edu.cstad.stackquizapi.exception.ApiResponse;
import kh.edu.cstad.stackquizapi.repository.UserRepository;
import kh.edu.cstad.stackquizapi.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final WebClient webClient;

    private final UserRepository userRepository;

    @Value("${keycloak.token-url}")
    private String keycloakTokenUrl;

    @Value("${keycloak.token-client-id}")
    private String clientId;

    @Value("${keycloak.token-client-secret}")
    private String clientSecret;

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

        if (userRepository.existsByUsernameAndIsDeletedTrue(request.username())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Your account has been disabled. Please contact support.");
        }

        try {
            Mono<LoginResponse> tokenMono = webClient.post()
                    .uri(keycloakTokenUrl)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(BodyInserters
                            .fromFormData("grant_type", "password")
                            .with("client_id", clientId)
                            .with("client_secret", clientSecret)
                            .with("username", request.username())
                            .with("password", request.password()))
                    .retrieve()
                    .bodyToMono(LoginResponse.class);

            LoginResponse loginResponse = tokenMono.block();

            return ResponseEntity.ok(ApiResponse.<LoginResponse>builder()
                    .success(true)
                    .message("Login successful")
                    .data(loginResponse)
                    .build());

        } catch (WebClientResponseException ex) {
            log.error("Keycloak login failed: {}", ex.getResponseBodyAsString());
            return ResponseEntity.status(ex.getStatusCode())
                    .body(ApiResponse.<LoginResponse>builder()
                            .success(false)
                            .message("Login failed: " + ex.getResponseBodyAsString())
                            .build());
        }
    }

    @PostMapping("/oauth/register")
    public ResponseEntity<RegisterResponse> oauthRegister(@Valid @RequestBody OAuthRegisterRequest request) {
        log.info("OAuth registration request received for email: {}", request.email());
        return authService.oauthRegister(request);
    }


}