package com.novacore.user.service;

import com.novacore.user.dto.CreateUserRequest;
import com.novacore.user.dto.UpdateUserRequest;
import com.novacore.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto createUser(CreateUserRequest request);

    UserDto getUserById(Long id);

    List<UserDto> getAllUsers();

    UserDto updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);
}

