package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.User;
import kh.edu.cstad.stackquizapi.dto.request.CreateUserRequest;
import kh.edu.cstad.stackquizapi.dto.request.UpdateUserRequest;
import kh.edu.cstad.stackquizapi.dto.response.UserResponse;
import kh.edu.cstad.stackquizapi.mapper.UserMapper;
import kh.edu.cstad.stackquizapi.repository.UserRepository;
import kh.edu.cstad.stackquizapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final Keycloak adminKeycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @Override
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        try {
            if (userRepository.existsByEmail(createUserRequest.email())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Email already exists"
                );
            }
            if (createUserRequest.firstName().isEmpty() || createUserRequest.lastName().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "First name or last name cannot be empty");
            }
            if (createUserRequest.email().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email cannot be empty");
            }

            User user = userMapper.fromCreateUserRequest(createUserRequest);
            user.setIsActive(true);
            user.setCreatedAt(LocalDateTime.now());
            User savedUser = userRepository.save(user);
            return userMapper.toUserResponse(savedUser);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating user", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error occurred");
        }
    }

    @Override
    public UserResponse getCurrentUser(Jwt accessToken) {

        String userId = accessToken.getSubject();
        log.info("User ID from access token: {}", userId);

        try {
            if (userId == null || userId.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "User ID cannot be null or empty"
                );
            }
            return userRepository.findByIdAndIsDeletedFalse(userId)
                    .map(userMapper::toUserResponse)
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "User id not found")
                    );
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while fetching user by id {}", userId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error occurred");
        }
    }

    @Override
    public UserResponse updateUser(Jwt accessToken, UpdateUserRequest updateUserRequest) {
        String userId = accessToken.getSubject();
        log.info("User ID from jwt access token: {}", userId);

        try {
            if (userId == null || userId.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "User ID cannot be null or empty");
            }

            // Get user from database
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // Get user from Keycloak
            UserRepresentation keycloakUser = adminKeycloak.realm(realm)
                    .users()
                    .get(userId)
                    .toRepresentation();

            // Check email uniqueness if email is being updated
            if (updateUserRequest.email() != null && !user.getEmail().equals(updateUserRequest.email())) {
                // Check in database
                if (userRepository.existsByEmail(updateUserRequest.email())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
                }
                // Check in Keycloak
                List<UserRepresentation> existingEmails = adminKeycloak.realm(realm)
                        .users()
                        .searchByEmail(updateUserRequest.email(), true);
                if (!existingEmails.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered in Keycloak");
                }
            }

            // Update Keycloak user
            if (updateUserRequest.email() != null) {
                keycloakUser.setEmail(updateUserRequest.email());
            }
            if (updateUserRequest.firstName() != null) {
                keycloakUser.setFirstName(updateUserRequest.firstName());
            }
            if (updateUserRequest.lastName() != null) {
                keycloakUser.setLastName(updateUserRequest.lastName());
            }

            try {
                adminKeycloak.realm(realm).users().get(userId).update(keycloakUser);
                log.info("User updated in Keycloak: {}", userId);
            } catch (Exception e) {
                log.error("Failed to update user in Keycloak: {}", e.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to update user in Keycloak");
            }

            // Update database user
            userMapper.toCustomerPartially(updateUserRequest, user);
            user = userRepository.save(user);
            log.info("User updated in database: {}", userId);

            return userMapper.toUserResponse(user);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while updating user with id {}", userId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error occurred");
        }
    }

    @Override
    public UserResponse updateUserByAdmin(String userId, UpdateUserRequest updateUserRequest) {
        log.info("Admin updating user with ID: {}", userId);

        try {
            if (userId == null || userId.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "User ID cannot be null or empty");
            }

            // Get user from database
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // Get user from Keycloak
            UserRepresentation keycloakUser;
            try {
                keycloakUser = adminKeycloak.realm(realm)
                        .users()
                        .get(userId)
                        .toRepresentation();
            } catch (Exception e) {
                log.error("Failed to get user from Keycloak: {}", e.getMessage());
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found in Keycloak");
            }

            // Check email uniqueness if email is being updated
            if (updateUserRequest.email() != null && !user.getEmail().equals(updateUserRequest.email())) {
                // Check in database
                if (userRepository.existsByEmail(updateUserRequest.email())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
                }
                // Check in Keycloak
                List<UserRepresentation> existingEmails = adminKeycloak.realm(realm)
                        .users()
                        .searchByEmail(updateUserRequest.email(), true);
                if (!existingEmails.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered in Keycloak");
                }
            }

            // Update Keycloak user
            if (updateUserRequest.email() != null) {
                keycloakUser.setEmail(updateUserRequest.email());
            }
            if (updateUserRequest.firstName() != null) {
                keycloakUser.setFirstName(updateUserRequest.firstName());
            }
            if (updateUserRequest.lastName() != null) {
                keycloakUser.setLastName(updateUserRequest.lastName());
            }

            try {
                adminKeycloak.realm(realm).users().get(userId).update(keycloakUser);
                log.info("Admin updated user in Keycloak: {}", userId);
            } catch (Exception e) {
                log.error("Admin failed to update user in Keycloak: {}", e.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to update user in Keycloak");
            }

            // Update database user
            userMapper.toCustomerPartially(updateUserRequest, user);
            user = userRepository.save(user);
            log.info("Admin updated user in database: {}", userId);

            return userMapper.toUserResponse(user);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while admin updating user with id {}", userId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error occurred");
        }
    }

    @Override
    public void deleteUserByUserId(String userId) {
        try {
            if (userId == null || userId.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "User ID cannot be null or empty");
            }
            if (!userRepository.existsById(userId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User ID not found");
            }
            userRepository.deleteById(userId);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while deleting user with id {}", userId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal error occurred");
        }
    }

    @Transactional
    @Override
    public void disableUserByUserId(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "User with ID: " + userId + " not found")
                );

        user.setIsDeleted(true);
    }

    @Override
    public List<UserResponse> findAll() {
        try {
            List<User> users = userRepository.findAllByIsDeletedFalse();

            if (users.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found");
            }
            return users.stream()
                    .map(userMapper::toUserResponse)
                    .toList();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while fetching users", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error occurred");
        }
    }
}