package com.novacore.user.service.impl;

import com.novacore.shared.constants.ErrorCode;
import com.novacore.shared.exception.BusinessException;
import com.novacore.shared.exception.ResourceNotFoundException;
import com.novacore.user.domain.User;
import com.novacore.user.dto.CreateUserRequest;
import com.novacore.user.dto.UpdateUserRequest;
import com.novacore.user.dto.UserDto;
import com.novacore.user.mapper.UserMapper;
import com.novacore.user.repository.UserRepository;
import com.novacore.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(
                ErrorCode.USER_400_USERNAME_EXISTS,
                String.format("Username '%s' already exists", request.getUsername())
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                ErrorCode.USER_400_EMAIL_EXISTS,
                String.format("Email '%s' already exists", request.getEmail())
            );
        }

        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);

        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    ErrorCode.USER_404_NOT_FOUND,
                    String.format("User with ID %d not found", id)
                ));

        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    ErrorCode.USER_404_NOT_FOUND,
                    String.format("User with ID %d not found", id)
                ));

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsernameExcludingId(request.getUsername(), id)) {
                throw new BusinessException(
                    ErrorCode.USER_400_USERNAME_EXISTS,
                    String.format("Username '%s' already exists", request.getUsername())
                );
            }
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailExcludingId(request.getEmail(), id)) {
                throw new BusinessException(
                    ErrorCode.USER_400_EMAIL_EXISTS,
                    String.format("Email '%s' already exists", request.getEmail())
                );
            }
        }

        userMapper.updateEntity(user, request);
        User updatedUser = userRepository.save(user);

        return userMapper.toDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                ErrorCode.USER_404_NOT_FOUND,
                String.format("User with ID %d not found", id)
            );
        }

        userRepository.deleteById(id);
    }
}

