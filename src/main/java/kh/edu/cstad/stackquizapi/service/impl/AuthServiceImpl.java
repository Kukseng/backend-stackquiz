package kh.edu.cstad.stackquizapi.service.impl;

import jakarta.ws.rs.core.Response;
import kh.edu.cstad.stackquizapi.dto.request.CreateUserRequest;
import kh.edu.cstad.stackquizapi.dto.request.OAuthRegisterRequest;
import kh.edu.cstad.stackquizapi.dto.request.RegisterRequest;
import kh.edu.cstad.stackquizapi.dto.request.ResetPasswordRequest;
import kh.edu.cstad.stackquizapi.dto.response.RegisterResponse;
import kh.edu.cstad.stackquizapi.dto.response.TokenResponse;
import kh.edu.cstad.stackquizapi.dto.response.UserResponse;
import kh.edu.cstad.stackquizapi.repository.UserRepository;
import kh.edu.cstad.stackquizapi.service.AuthService;
import kh.edu.cstad.stackquizapi.service.RoleService;
import kh.edu.cstad.stackquizapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final Keycloak keycloak;
    private final Keycloak adminKeycloak;
    private final RoleService roleService;
    private final UserService userService;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    private static final Pattern SAFE_CHAR = Pattern.compile("[a-z0-9._-]");

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

    @Value("${keycloak.token-client-id}")
    private String tokenClientId;

    @Value("${keycloak.token-client-secret:}")
    private String tokenClientSecret;

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
                            .isDeleted(false)
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
    public void requestPasswordReset(ResetPasswordRequest resetPasswordRequest) {
        // Find the user by email
        UserRepresentation user = keycloak.realm(realm)
                .users()
                .searchByEmail(resetPasswordRequest.email(), true)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Send Link to user for reset password
        getUserResource(user.getId()).executeActionsEmail(List.of("UPDATE_PASSWORD"));
    }


    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        // Find the user by email
        UserRepresentation user = keycloak.realm(realm)
                .users()
                .searchByEmail(resetPasswordRequest.email(), true)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email"));

        // Get the UserResource and reset the password
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(resetPasswordRequest.newPassword());
        credential.setTemporary(false);

        getUserResource(user.getId()).resetPassword(credential);
    }



    /** Lowercase,  collapse repeats, trim */
    private String sanitizeUsername(String raw, String email) {
        String base = (raw != null && !raw.isBlank()) ? raw : (email != null ? email.split("@")[0] : "user");

        String lowered = base.toLowerCase(Locale.ROOT);
        String cleaned = lowered.chars()
                .mapToObj(c -> {
                    char ch = (char) c;
                    return SAFE_CHAR.matcher(Character.toString(ch)).matches()
                            ? Character.toString(ch)
                            : "-";
                })
                .collect(Collectors.joining());

        cleaned = cleaned.replaceAll("[-_.]{2,}", "-");
        cleaned = cleaned.replaceAll("^[-_.]+|[-_.]+$", "");

        if (cleaned.isBlank()) cleaned = "user";
        if (cleaned.length() < 3) cleaned = (cleaned + "-user");
        if (cleaned.length() > 30) cleaned = cleaned.substring(0, 30);
        return cleaned;
    }

    /** Check KC for conflicts and append short suffix  */
    private String makeUniqueUsername(String base) {
        String candidate = base;
        int attempt = 0;
        while (true) {
            List<UserRepresentation> found = adminKeycloak.realm(realm).users().search(candidate, true);
            String finalCandidate = candidate;
            boolean exists = found.stream().anyMatch(u -> finalCandidate.equalsIgnoreCase(u.getUsername()));
            if (!exists) return candidate;

            attempt++;
            String suffix = "-" + Integer.toString(attempt, 36);
            int maxBaseLen = Math.max(1, 30 - suffix.length());
            candidate = (base.length() > maxBaseLen ? base.substring(0, maxBaseLen) : base) + suffix;
        }
    }

    @Override
    public ResponseEntity<RegisterResponse> oauthRegister(OAuthRegisterRequest request) {
        if (request == null || request.email() == null || request.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        final String email = request.email();
        log.info("Starting OAuth registration for user: {}", email);

        List<UserRepresentation> existingUsers = adminKeycloak.realm(realm)
                .users()
                .searchByEmail(email, true);

        String userId;
        String kcUsername;
        String randomPassword = UUID.randomUUID().toString();
        TokenResponse token;

        if (!existingUsers.isEmpty()) {
            // ===== EXISTING user in KC =====
            UserRepresentation existingUser = existingUsers.get(0);
            userId = existingUser.getId();
            kcUsername = existingUser.getUsername();
            log.info("User already exists in Keycloak: {}", userId);

            // ensure enabled & verified
            try {
                existingUser.setEnabled(true);
                existingUser.setEmailVerified(true);
                adminKeycloak.realm(realm).users().get(userId).update(existingUser);
            } catch (Exception e) {
                log.warn("Failed to update KC flags for {}: {}", userId, e.getMessage());
            }

            // ensure default role (idempotent)
            try {
                roleService.assignRole(userId, defaultRole);
            } catch (Exception e) {
                log.warn("Role assignment skipped/failed for {}: {}", userId, e.getMessage());
            }

            // UPSERT DB user if missing
            try {
                if (userRepository.findById(userId).isEmpty()) {
                    userService.createUser(CreateUserRequest.builder()
                            .id(userId)
                            .username(kcUsername)
                            .email(existingUser.getEmail())
                            .firstName(existingUser.getFirstName())
                            .lastName(existingUser.getLastName())
                            .build());
                    log.info("App DB user created for existing KC user {}", userId);
                }
            } catch (Exception e) {
                log.error("Failed to upsert app DB user for existing KC user {}: {}", userId, e.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upsert application user");
            }

            // set random password
            try {
                CredentialRepresentation cred = new CredentialRepresentation();
                cred.setType(CredentialRepresentation.PASSWORD);
                cred.setTemporary(false);
                cred.setValue(randomPassword);
                adminKeycloak.realm(realm).users().get(userId).resetPassword(cred);
            } catch (Exception e) {
                log.error("Failed to reset password for KC user {}: {}", userId, e.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to prepare user for token issuance");
            }

        } else {
            // ===== NEW user in KC =====
            String desired = (request.username() != null && !request.username().isBlank())
                    ? request.username()
                    : email;
            kcUsername = makeUniqueUsername(sanitizeUsername(desired, email));

            UserRepresentation user = new UserRepresentation();
            user.setUsername(kcUsername);
            user.setEmail(email);
            user.setFirstName(request.firstName());
            user.setLastName(request.lastName());
            user.setEnabled(true);
            user.setEmailVerified(true);

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setTemporary(false);
            credential.setValue(randomPassword);
            user.setCredentials(List.of(credential));

            try (Response response = adminKeycloak.realm(realm).users().create(user)) {
                if (response.getStatus() != HttpStatus.CREATED.value()) {
                    String body;
                    try { body = response.readEntity(String.class); } catch (Exception ex) { body = "<no body>"; }
                    log.error("KC create failed: status={}, body={}", response.getStatus(), body);
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user in Keycloak");
                }
                String locationHeader = response.getHeaderString("Location");
                if (locationHeader == null) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No Location header from KC");
                }
                userId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
                log.info("User created in Keycloak with ID {}", userId);
            }

            // assign default role
            try {
                roleService.assignRole(userId, defaultRole);
            } catch (Exception e) {
                log.warn("Role assignment failed for {}: {}", userId, e.getMessage());
            }

            // save to  DB
            try {
                userService.createUser(CreateUserRequest.builder()
                        .id(userId)
                        .username(kcUsername)
                        .email(email)
                        .firstName(request.firstName())
                        .lastName(request.lastName())
                        .build());
                log.info("App DB user created with ID: {}", userId);
            } catch (Exception e) {
                log.error("Failed to save user to DB for KC user {}: {}", userId, e.getMessage());
                try { adminKeycloak.realm(realm).users().get(userId).remove(); }
                catch (Exception ex) { log.error("Rollback KC user failed for {}: {}", userId, ex.getMessage()); }
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to complete user registration");
            }

            // finally fetch token
        }
        token = getTokenFromKeycloak(kcUsername, randomPassword);

        RegisterResponse body = RegisterResponse.builder()
                .userId(userId)
                .email(email)
                .username(kcUsername)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .emailVerified(true)
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .build();

        return ResponseEntity.ok(body);
    }

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper =
            new com.fasterxml.jackson.databind.ObjectMapper()
                    .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

    private TokenResponse getTokenFromKeycloak(String username, String password) {
        final String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> req = getMultiValueMapHttpEntity(username, password, headers);

        ResponseEntity<String> raw = restTemplate.postForEntity(tokenUrl, req, String.class);

        if (raw.getStatusCode() != HttpStatus.OK || raw.getBody() == null) {
            log.error("Keycloak token fetch failed: status={}, body={}", raw.getStatusCode(), raw.getBody());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Failed to get token from Keycloak");
        }

        try {
            return objectMapper.readValue(raw.getBody(), TokenResponse.class);
        } catch (Exception e) {
            log.error("Keycloak token parse error: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Token retrieval error");
        }
    }

    private HttpEntity<MultiValueMap<String, String>> getMultiValueMapHttpEntity(String username, String password, HttpHeaders headers) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "password");
        form.add("client_id", tokenClientId);
        if (tokenClientSecret != null && !tokenClientSecret.isBlank()) {
            form.add("client_secret", tokenClientSecret);
        }
        form.add("username", username);
        form.add("password", password);

        return new HttpEntity<>(form, headers);
    }

    private UserResource getUserResource(String userId) {
        try {
            return keycloak.realm(realm).users().get(userId);
        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found in Keycloak");
        }
    }

}