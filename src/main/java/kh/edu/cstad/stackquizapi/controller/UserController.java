package kh.edu.cstad.stackquizapi.controller;

import kh.edu.cstad.stackquizapi.dto.request.CreateUserRequest;
import kh.edu.cstad.stackquizapi.dto.request.UpdateUserRequest;
import kh.edu.cstad.stackquizapi.dto.response.UserResponse;
import kh.edu.cstad.stackquizapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController{

    private final UserService userService;

    @PostMapping
    private UserResponse createUser(@RequestBody CreateUserRequest createUserRequest) {
        return userService.createUser(createUserRequest);
    }

    @GetMapping
    private List<UserResponse> findAll() {
        return userService.findAll();
    }

    @DeleteMapping("/{userId}")
    private void deleteUserByUserId(@PathVariable String userId) {
        userService.deleteUserByUserId(userId);
    }

    @GetMapping("/{userId}")
    private UserResponse getUserById(@PathVariable String userId) {
        return userService.getUserById(userId);
    }

    @PutMapping("/{userId}")
    private UserResponse updateUserByUserId(@PathVariable String userId,
                                            @RequestBody UpdateUserRequest updateUserRequest) {
        return userService.updateUserByUserId(userId, updateUserRequest);
    }

}
