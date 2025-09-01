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
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final Keycloak adminKeycloak;
    private final RoleService roleService;
    private final UserService userService;

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

                userService.createUser(CreateUserRequest
                        .builder()
                                .firstName(createdUser.getFirstName())
                                .lastName(createdUser.getLastName())
                                .email(createdUser.getEmail())
                                .username(createdUser.getUsername())
                        .build());

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
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.username());

        try {
            KeycloakBuilder builder = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realm)
                    .clientId(clientId)
                    .username(request.username())
                    .password(request.password());

            if (clientSecret != null && !clientSecret.isEmpty()) {
                builder.clientSecret(clientSecret);
            }

            Keycloak userKeycloak = builder.build();

            AccessTokenResponse tokenResponse = userKeycloak.tokenManager().getAccessToken();

            if (tokenResponse == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
            }

            UserRepresentation user = getUserByUsername(request.username());

            if (!user.isEnabled()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is disabled");
            }

            List<String> roles = getUserRoles(user.getId());

            log.info("User {} successfully authenticated", request.username());

            return LoginResponse.builder()
                    .accessToken(tokenResponse.getToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .tokenType(tokenResponse.getTokenType())
                    .expiresIn(tokenResponse.getExpiresIn())
                    .refreshExpiresIn(tokenResponse.getRefreshExpiresIn())
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .roles(roles)
                    .emailVerified(user.isEmailVerified())
                    .build();

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Login failed for user {}: {}", request.username(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refreshing token");

        try {
            String tokenEndpoint = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "refresh_token");
            formData.add("refresh_token", request.refreshToken());
            formData.add("client_id", clientId);

            if (clientSecret != null && !clientSecret.isEmpty()) {
                formData.add("client_secret", clientSecret);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(
                    tokenEndpoint,
                    HttpMethod.POST,
                    requestEntity,
                    AccessTokenResponse.class
            );

            AccessTokenResponse tokenResponse = response.getBody();

            if (tokenResponse == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
            }

            log.info("Token successfully refreshed");

            return LoginResponse.builder()
                    .accessToken(tokenResponse.getToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .tokenType(tokenResponse.getTokenType())
                    .expiresIn(tokenResponse.getExpiresIn())
                    .refreshExpiresIn(tokenResponse.getRefreshExpiresIn())
                    .build();

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token refresh failed");
        }
    }

    @Override
    public void logout(LogoutRequest request) {
        log.info("Logging out user: {}", request.userId());

        try {
            UserResource userResource = adminKeycloak.realm(realm)
                    .users()
                    .get(request.userId());

            userResource.logout();

            log.info("User {} successfully logged out", request.userId());

        } catch (Exception e) {
            log.error("Logout failed for user {}: {}", request.userId(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Logout failed");
        }
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

    @Override
    public void requestPasswordReset(String email) {
        log.info("Password reset requested for email: {}", email);

        try {
            UserRepresentation user = adminKeycloak.realm(realm)
                    .users()
                    .searchByEmail(email, true)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "No account found with this email"));

            adminKeycloak.realm(realm)
                    .users()
                    .get(user.getId())
                    .executeActionsEmail(Collections.singletonList("UPDATE_PASSWORD"));

            log.info("Password reset email sent for user: {}", user.getId());

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to send password reset email: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to send password reset email");
        }
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Resetting password for email: {}", request.email());

        try {
            UserRepresentation user = adminKeycloak.realm(realm)
                    .users()
                    .searchByEmail(request.email(), true)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "No account found with this email"));

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(request.newPassword());
            credential.setTemporary(false);

            adminKeycloak.realm(realm)
                    .users()
                    .get(user.getId())
                    .resetPassword(credential);

            log.info("Password successfully reset for user: {}", user.getId());

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Password reset failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Password reset failed");
        }
    }

    @Override
    public UserProfileResponse getUserProfile(String userId) {
        try {
            UserRepresentation user = adminKeycloak.realm(realm)
                    .users()
                    .get(userId)
                    .toRepresentation();

            List<String> roles = getUserRoles(userId);

            return UserProfileResponse.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .emailVerified(user.isEmailVerified())
                    .enabled(user.isEnabled())
                    .roles(roles)
                    .createdTimestamp(user.getCreatedTimestamp())
                    .build();

        } catch (Exception e) {
            log.error("Failed to get user profile for {}: {}", userId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    private UserRepresentation getUserByUsername(String username) {
        return adminKeycloak.realm(realm)
                .users()
                .search(username, true)
                .stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private List<String> getUserRoles(String userId) {
        try {
            return adminKeycloak.realm(realm)
                    .users()
                    .get(userId)
                    .roles()
                    .realmLevel()
                    .listAll()
                    .stream()
                    .map(RoleRepresentation::getName)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to get roles for user {}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }
}