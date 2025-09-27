package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(UserDto userDto) {
        log.debug("Creating user with email: {}", userDto != null ? userDto.getEmail() : null);
        if (userDto == null) {
            log.warn("UserDto is null");
            throw new IllegalArgumentException("UserDto cannot be null");
        }
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            log.warn("Email is null or blank");
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            log.warn("Name is null or blank");
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            log.warn("Email already exists: {}", userDto.getEmail());
            throw new ConflictException("Email already exists: " + userDto.getEmail());
        }
        try {
            User user = userMapper.toUser(userDto);
            if (user == null) {
                log.error("Failed to map UserDto to User for email: {}", userDto.getEmail());
                throw new IllegalStateException("Failed to map UserDto to User");
            }
            User savedUser = userRepository.save(user);
            UserDto result = userMapper.toUserDto(savedUser);
            if (result == null) {
                log.error("Failed to map saved User to UserDto for id: {}", savedUser.getId());
                throw new IllegalStateException("Failed to map User to UserDto");
            }
            log.info("User created: id={} email={}", result.getId(), result.getEmail());
            return result;
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.debug("Updating user with id: {}", userId);
        if (userId == null) {
            log.warn("User ID is null");
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (userDto == null) {
            log.warn("UserDto is null");
            throw new IllegalArgumentException("UserDto cannot be null");
        }
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", userId);
                    return new NoSuchElementException("User not found: " + userId);
                });
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank() && !userDto.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(userDto.getEmail())) {
                log.warn("Email already exists: {}", userDto.getEmail());
                throw new ConflictException("Email already exists: " + userDto.getEmail());
            }
            existingUser.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            existingUser.setName(userDto.getName());
        }
        User updatedUser = userRepository.save(existingUser);
        UserDto result = userMapper.toUserDto(updatedUser);
        log.info("User updated: id={} email={}", result.getId(), result.getEmail());
        return result;
    }

    @Override
    public UserDto getUser(Long userId) {
        log.debug("Getting user with id: {}", userId);
        if (userId == null) {
            log.warn("User ID is null");
            throw new IllegalArgumentException("User ID cannot be null");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", userId);
                    return new NoSuchElementException("User not found: " + userId);
                });
        UserDto result = userMapper.toUserDto(user);
        if (result == null) {
            log.error("Failed to map User to UserDto for id: {}", userId);
            throw new IllegalStateException("Failed to map User to UserDto");
        }
        return result;
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.debug("Getting all users");
        return userRepository.findAll()
                .stream()
                .map(user -> {
                    UserDto dto = userMapper.toUserDto(user);
                    if (dto == null) {
                        log.error("Failed to map User to UserDto for id: {}", user.getId());
                        throw new IllegalStateException("Failed to map User to UserDto");
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        log.debug("Deleting user with id: {}", userId);
        if (userId == null) {
            log.warn("User ID is null");
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (!userRepository.existsById(userId)) {
            log.warn("User not found: {}", userId);
            throw new NoSuchElementException("User not found: " + userId);
        }
        userRepository.deleteById(userId);
        log.info("User deleted: id={}", userId);
    }
}