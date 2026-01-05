package com.novacore.user.controller;

import com.novacore.shared.constants.ApiConstants;
import com.novacore.shared.response.ApiResponse;
import com.novacore.shared.response.ApiResponseBuilder;
import com.novacore.user.dto.CreateUserRequest;
import com.novacore.user.dto.UpdateUserRequest;
import com.novacore.user.dto.UserDto;
import com.novacore.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.USERS_ENDPOINT)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserDto>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDto userDto = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseBuilder.success("User created successfully", userDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        UserDto userDto = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponseBuilder.success("User retrieved successfully", userDto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponseBuilder.success("Users retrieved successfully", users));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserDto userDto = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponseBuilder.success("User updated successfully", userDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponseBuilder.success("User deleted successfully"));
    }
}

