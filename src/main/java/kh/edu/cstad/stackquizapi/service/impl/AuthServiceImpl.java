package kh.edu.cstad.stackquizapi.service.impl;

import jakarta.ws.rs.core.Response;
import kh.edu.cstad.stackquizapi.dto.request.CreateUserRequest;
import kh.edu.cstad.stackquizapi.dto.request.RegisterRequest;
import kh.edu.cstad.stackquizapi.dto.request.ResetPasswordRequest;
import kh.edu.cstad.stackquizapi.dto.response.RegisterResponse;
import kh.edu.cstad.stackquizapi.dto.response.UserResponse;
import kh.edu.cstad.stackquizapi.service.AuthService;
import kh.edu.cstad.stackquizapi.service.RoleService;
import kh.edu.cstad.stackquizapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

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
                    CreateUserRequest createUserRequest = CreateUserRequest.builder()
                            .id(createdUser.getId())
                            .username(request.username())
                            .firstName(request.firstName())
                            .lastName(request.lastName())
                            .email(request.email())
                            .build();

                    UserResponse dbUser = userService.createUser(createUserRequest);
                    log.info("User saved to database with ID: {}", dbUser.id());
                } catch (Exception e) {
                    log.error("Failed to save user to database: {}", e.getMessage());

                    try {
                        adminKeycloak.realm(realm).users().get(userId).remove();
                        log.warn("Rolled back Keycloak user creation due to database save failure");
                    } catch (Exception rollbackException) {
                        log.error("Failed to rollback Keycloak user creation: {}", rollbackException.getMessage());
                    }
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to complete user registration: " + e.getMessage());
                }

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

        } catch (Exception e) {
            log.error("Unexpected error during registration: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Registration failed: " + e.getMessage());
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
}