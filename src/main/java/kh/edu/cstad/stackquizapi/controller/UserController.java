package kh.edu.cstad.stackquizapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kh.edu.cstad.stackquizapi.dto.request.UpdateUserRequest;
import kh.edu.cstad.stackquizapi.dto.response.UserResponse;
import kh.edu.cstad.stackquizapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // get current user for profile
//    @Operation(summary = "Get all users (secured)",
//            security = {@SecurityRequirement(name = "bearerAuth")})
//    @ResponseStatus(HttpStatus.CREATED)
//    @PostMapping
//    public UserResponse createUser(@RequestBody CreateUserRequest createUserRequest) {
//        return userService.createUser(createUserRequest);
//    }

    //    @SecurityRequirement(name = "bearerAuth")
//    @Tag(name = "User", description = "The User API. Contains all the operations that can be performed on a user.")
// get current user for profile
    @Operation(summary = "Get all users (secured)",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    // get current user for profile
    @Operation(summary = "Delete user by ID (secured)",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{userId}")
    public void deleteUserByUserId(@PathVariable String userId) {
        userService.deleteUserByUserId(userId);
    }

    // get current user for profile
    @Operation(summary = "Get current user profile (secured)",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal Jwt accessToken) {
        return userService.getCurrentUser(accessToken);
    }

    @Operation(summary = "Update current user (secured)",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/me")
    public UserResponse updateUser(@AuthenticationPrincipal Jwt accessToken,
                                           @RequestBody UpdateUserRequest updateUserRequest) {
        return userService.updateUser(accessToken, updateUserRequest);
    }
}
