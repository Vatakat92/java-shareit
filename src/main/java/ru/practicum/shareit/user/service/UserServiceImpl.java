package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserServiceImpl implements UserService {
    private static final Comparator<User> BY_ID = Comparator.comparing(User::getId);
    private final UserRepository userRepository;

    private void validateUserDtoNotNull(UserDto dto) {
        if (dto == null) {
            log.error("User DTO cannot be null");
            throw new IllegalArgumentException("User DTO cannot be null");
        }
    }

    private void validateIdNotNull(Long id) {
        if (id == null) {
            log.error("User ID cannot be null");
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }

    private User getExistingUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", id);
                    return new NoSuchElementException("User not found with id: " + id);
                });
    }

    @Override
    public UserDto createUser(UserDto dto) {
        validateUserDtoNotNull(dto);
        log.info("Create user: email={}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("User with email {} already exists", dto.getEmail());
            throw new ConflictException("User with email " + dto.getEmail() + " already exists");
        }

        User savedUser = userRepository.save(UserMapper.toUser(dto));
        log.info("User created: id={} email={}", savedUser.getId(), savedUser.getEmail());
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto updateUser(Long id, UserDto dto) {
        validateUserDtoNotNull(dto);
        log.info("Update user: id={}", id);

        User existingUser = getExistingUser(id);

        boolean changed = false;

        if (dto.getEmail() != null && !dto.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                log.warn("Email {} is already in use", dto.getEmail());
                throw new ConflictException("Email " + dto.getEmail() + " is already in use");
            }
            existingUser.setEmail(dto.getEmail());
            changed = true;
        }
        if (dto.getName() != null && !dto.getName().equals(existingUser.getName())) {
            existingUser.setName(dto.getName());
            changed = true;
        }

        if (!changed) {
            log.debug("No changes for user id={}, skipping save", id);
            return UserMapper.toUserDto(existingUser);
        }

        userRepository.save(existingUser);
        log.info("User updated: id={}", id);
        return UserMapper.toUserDto(existingUser);
    }

    @Override
    public UserDto getUserById(Long id) {
        validateIdNotNull(id);
        log.debug("Get user: id={}", id);

        User user = getExistingUser(id);
        log.debug("Got user: id={}", id);
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.debug("Get users list");
        List<UserDto> users = userRepository.findAll().stream()
                .sorted(BY_ID)
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
        log.debug("Users found: {}", users.size());
        return users;
    }

    @Override
    public void deleteUser(Long id) {
        validateIdNotNull(id);
        log.info("Delete user: id={}", id);

        if (!userRepository.existsById(id)) {
            log.warn("User not found for deletion: {}", id);
            throw new NoSuchElementException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
        log.info("User deleted: id={}", id);
    }
}

