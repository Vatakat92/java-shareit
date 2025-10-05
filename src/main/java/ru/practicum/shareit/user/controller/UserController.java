package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.validation.Create;
import ru.practicum.shareit.validation.Update;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Validated(Create.class) @RequestBody UserDto dto) {
        log.info("User API create: email={}", dto != null ? dto.getEmail() : null);
        UserDto created = userService.createUser(dto);
        if (created == null) {
            log.error("UserService returned null for createUser");
            throw new IllegalStateException("Failed to create user: service returned null");
        }
        URI location = URI.create("/users/" + created.getId());
        log.info("User API created: id={} email={}", created.getId(), created.getEmail());
        return ResponseEntity.created(location).body(created);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id,
                                              @Validated(Update.class) @RequestBody UserDto dto) {
        log.info("User API update: id={}", id);
        UserDto updatedUser = userService.updateUser(id, dto);
        log.info("User API updated: id={} email={}", updatedUser.getId(), updatedUser.getEmail());
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        log.debug("User API get by id: {}", id);
        UserDto user = userService.getUser(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.debug("User API list all");
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("User API delete: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}