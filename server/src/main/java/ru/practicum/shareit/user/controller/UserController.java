package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody UserCreateDto dto) {
        log.info("User API create: email={}", dto != null ? dto.getEmail() : null);
        UserDto created = userService.createUser(dto);
        log.info("User API created: id={} email={}", created.getId(), created.getEmail());
        return created;
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable Long id,
                              @RequestBody UserUpdateDto dto) {
        log.info("User API update: id={}, dto={}", id, dto);
        UserDto updatedUser = userService.updateUser(id, dto);
        log.info("User API updated: id={} email={}", updatedUser.getId(), updatedUser.getEmail());
        return updatedUser;
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable Long id) {
        log.debug("User API get by id: {}", id);
        return userService.getUser(id);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.debug("User API list all");
        return userService.getAllUsers();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        log.info("User API delete: {}", id);
        userService.deleteUser(id);
    }
}