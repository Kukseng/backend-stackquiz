package kh.edu.cstad.stackquizapi.service.impl;

import jakarta.ws.rs.core.Response;
import kh.edu.cstad.stackquizapi.dto.request.LoginRequest;
import kh.edu.cstad.stackquizapi.dto.request.RegisterRequest;
import kh.edu.cstad.stackquizapi.dto.response.RegisterResponse;
import kh.edu.cstad.stackquizapi.service.AuthService;
import kh.edu.cstad.stackquizapi.service.RoleService;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final Keycloak keycloak;
    private final RoleService roleService;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${app.default-role:USER}")
    private String defaultRole;

    @Override
    public RegisterResponse register(RegisterRequest registerRequest) {

        if (!registerRequest.password().equals(registerRequest.confirmedPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Passwords don't match");
        }

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(registerRequest.username());
        userRepresentation.setEmail(registerRequest.email());
        userRepresentation.setFirstName(registerRequest.firstName());
        userRepresentation.setLastName(registerRequest.lastName());
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(false);

        CredentialRepresentation cr = new CredentialRepresentation();
        cr.setType(CredentialRepresentation.PASSWORD);
        cr.setValue(registerRequest.confirmedPassword());
        userRepresentation.setCredentials(List.of(cr));

        try (Response response = keycloak.realm(realm)
                .users()
                .create(userRepresentation)) {

            if (response.getStatus() == HttpStatus.CREATED.value()) {

                String locationHeader = response.getHeaderString("Location");
                if (locationHeader == null) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "No Location header in response");
                }

                String userId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
                log.info("Extracted user ID from Location header: {}", userId);

                UserRepresentation ur;
                try {
                    ur = keycloak.realm(realm)
                            .users()
                            .get(userId)
                            .toRepresentation();
                } catch (Exception e) {
                    log.error("Failed to retrieve user by ID {}: {}", userId, e.getMessage());
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to retrieve created user");
                }

                try {
                    roleService.assignRole(ur.getId(), defaultRole);
                    log.info("Role {} assigned to user {}", defaultRole, ur.getId());
                } catch (Exception e) {
                    log.warn("Failed to assign role to user {}: {}", ur.getId(), e.getMessage());
                }

                try {
                    this.verifyEmail(ur.getId());
                    log.info("Verification email sent to user {}", ur.getId());
                } catch (Exception e) {
                    log.warn("Failed to send verification email to user {}: {}", ur.getId(), e.getMessage());
                }

                return RegisterResponse.builder()
                        .userId(ur.getId())
                        .username(ur.getUsername())
                        .email(ur.getEmail())
                        .firstName(ur.getFirstName())
                        .lastName(ur.getLastName())
                        .build();
            }

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create user. Status: " + response.getStatus());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during registration: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Registration failed");
        }
    }

    @Override
    public void login(LoginRequest loginRequest) {

    }

    @Override
    public void verifyEmail(String userId) {
        try {
            UserResource userResource = keycloak.realm(realm)
                    .users().get(userId);
            userResource.sendVerifyEmail();
            log.info("Verification email sent for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to send verification email for user: {}", userId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to send verification email");
        }
    }
}