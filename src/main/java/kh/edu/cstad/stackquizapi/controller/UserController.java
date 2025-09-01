package kh.edu.cstad.stackquizapi.controller;

import kh.edu.cstad.stackquizapi.dto.request.CreateUserRequest;
import kh.edu.cstad.stackquizapi.dto.request.UpdateUserRequest;
import kh.edu.cstad.stackquizapi.dto.response.UserResponse;
import kh.edu.cstad.stackquizapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public UserResponse createUser(@RequestBody CreateUserRequest createUserRequest) {
        return userService.createUser(createUserRequest);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{userId}")
    public void deleteUserByUserId(@PathVariable String userId) {
        userService.deleteUserByUserId(userId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{userId}")
    public UserResponse getUserById(@PathVariable String userId) {
        return userService.getUserById(userId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{userId}")
    public UserResponse updateUserByUserId(@PathVariable String userId,
                                           @RequestBody UpdateUserRequest updateUserRequest) {
        return userService.updateUserByUserId(userId, updateUserRequest);
    }
}
