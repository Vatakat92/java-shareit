package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
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
    public UserDto createUser(UserCreateDto userDto) {
        log.debug("Creating user with email: {}", userDto != null ? userDto.getEmail() : null);

        if (userDto == null) {
            throw new IllegalArgumentException("UserDto cannot be null");
        }
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }

        if (userRepository.existsByEmail(userDto.getEmail())) {
            log.error("Email already exists: {}", userDto.getEmail());
            throw new ConflictException("Email already exists: " + userDto.getEmail());
        }

        User user = userMapper.toUser(userDto);
        User savedUser = userRepository.save(user);
        UserDto result = userMapper.toUserDto(savedUser);
        log.info("User created: id={} email={}", result.getId(), result.getEmail());
        return result;
    }

    @Override
    public UserDto updateUser(Long userId, UserUpdateDto userDto) {
        log.debug("Updating user with id: {}, userDto={}", userId, userDto);

        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (userDto == null) {
            throw new IllegalArgumentException("UserDto cannot be null");
        }

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            String newEmail = userDto.getEmail().trim();
            String currentEmail = existingUser.getEmail();

            if (!newEmail.equals(currentEmail)) {
                if (userRepository.existsByEmailAndIdNot(newEmail, userId)) {
                    log.warn("Email already exists: {}", newEmail);
                    throw new ConflictException("Email already exists: " + newEmail);
                }
                existingUser.setEmail(newEmail);
            }
        }

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            existingUser.setName(userDto.getName().trim());
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
            throw new IllegalArgumentException("User ID cannot be null");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        return userMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.debug("Getting all users");
        return userRepository.findAll()
                .stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        log.debug("Deleting user with id: {}", userId);
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("User not found: " + userId);
        }
        userRepository.deleteById(userId);
        log.info("User deleted: id={}", userId);
    }
}