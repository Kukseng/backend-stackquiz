package kh.edu.cstad.stackquizapi.service.impl;

import jakarta.ws.rs.core.Response;
import kh.edu.cstad.stackquizapi.dto.request.CreateUserRequest;
import kh.edu.cstad.stackquizapi.dto.request.LoginRequest;
import kh.edu.cstad.stackquizapi.dto.request.LogoutRequest;
import kh.edu.cstad.stackquizapi.dto.request.RefreshTokenRequest;
import kh.edu.cstad.stackquizapi.dto.request.RegisterRequest;
import kh.edu.cstad.stackquizapi.dto.request.ResetPasswordRequest;
import kh.edu.cstad.stackquizapi.dto.response.LoginResponse;
import kh.edu.cstad.stackquizapi.dto.response.RegisterResponse;
import kh.edu.cstad.stackquizapi.dto.response.UserProfileResponse;
import kh.edu.cstad.stackquizapi.service.AuthService;
import kh.edu.cstad.stackquizapi.service.RoleService;
import kh.edu.cstad.stackquizapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final Keycloak adminKeycloak;
    private final RoleService roleService;
    private final UserService userService;
    private final WebClient webClient = WebClient.builder().build();
    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret:}")
    private String clientSecret;

    @Value("${app.default-role}")
    private String defaultRole;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        log.info("Starting registration for user: {}", request.username());

        if (!request.password().equals(request.confirmedPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords don't match");
        }

        List<UserRepresentation> existingUsers = adminKeycloak.realm(realm)
                .users()
                .search(request.username(), true);

        if (!existingUsers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        List<UserRepresentation> existingEmails = adminKeycloak.realm(realm)
                .users()
                .searchByEmail(request.email(), true);

        if (!existingEmails.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(true);
        user.setEmailVerified(false);

        // Set password
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.password());
        credential.setTemporary(false);
        user.setCredentials(Collections.singletonList(credential));

        try (Response response = adminKeycloak.realm(realm).users().create(user)) {

            if (response.getStatus() == HttpStatus.CREATED.value()) {
                String locationHeader = response.getHeaderString("Location");
                if (locationHeader == null) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to get user ID from response");
                }

                String userId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
                log.info("User created with ID: {}", userId);

                UserRepresentation createdUser = adminKeycloak.realm(realm)
                        .users()
                        .get(userId)
                        .toRepresentation();

                try {
                    roleService.assignRole(createdUser.getId(), defaultRole);
                    log.info("Role {} assigned to user {}", defaultRole, createdUser.getId());
                } catch (Exception e) {
                    log.warn("Failed to assign role to user {}: {}", createdUser.getId(), e.getMessage());
                }

                try {
                    verifyEmail(userId);
                    log.info("Verification email sent to user {}", userId);
                } catch (Exception e) {
                    log.warn("Failed to send verification email: {}", e.getMessage());
                }

                return RegisterResponse.builder()
                        .userId(createdUser.getId())
                        .username(createdUser.getUsername())
                        .email(createdUser.getEmail())
                        .firstName(createdUser.getFirstName())
                        .lastName(createdUser.getLastName())
                        .emailVerified(createdUser.isEmailVerified())
                        .build();

            } else {
                log.error("User creation failed with status: {}", response.getStatus());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to create user. Status: " + response.getStatus());
            }

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during registration: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Registration failed: " + e.getMessage());
        }
    }


    @Override
    public Mono<String> login(String username, String password) {
        return webClient.post()
                .uri("/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData("client_id", "nextjs") // your client_id
                        .with("client_secret", "azpLBVVq454Vzz22h004FbTqeMGFS8k7")    // your client_secret
                        .with("grant_type", "password")
                        .with("username", username)
                        .with("password", password))
                .retrieve()
                .bodyToMono(String.class);
    }




    @Override
    public void verifyEmail(String userId) {
        try {
            UserResource userResource = adminKeycloak.realm(realm)
                    .users()
                    .get(userId);

            userResource.sendVerifyEmail();
            log.info("Verification email sent for user: {}", userId);

        } catch (Exception e) {
            log.error("Failed to send verification email for user {}: {}", userId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to send verification email");
        }
    }

}