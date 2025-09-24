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
        log.info("User ID form jwt access token: {}", userId);

        try {
            if (userId == null || userId.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "User ID cannot be null or empty");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            userMapper.toCustomerPartially(updateUserRequest, user);

            if (updateUserRequest.email() != null &&
                userRepository.existsByEmail(updateUserRequest.email()) &&
                !user.getEmail().equals(updateUserRequest.email())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
            }

            user = userRepository.save(user);
            return userMapper.toUserResponse(user);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while updating user with id {}", userId, e);
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