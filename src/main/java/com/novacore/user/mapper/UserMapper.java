package com.novacore.user.mapper;

import com.novacore.user.domain.User;
import com.novacore.user.dto.CreateUserRequest;
import com.novacore.user.dto.UpdateUserRequest;
import com.novacore.user.dto.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public User toEntity(CreateUserRequest request) {
        if (request == null) {
            return null;
        }

        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .status(User.UserStatus.ACTIVE)
                .build();
    }

    public void updateEntity(User user, UpdateUserRequest request) {
        if (user == null || request == null) {
            return;
        }

        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
    }
}





