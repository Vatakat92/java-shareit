package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.practicum.shareit.user.dto.UserDto;
import java.net.URI;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.validation.Create;
import ru.practicum.shareit.validation.Update;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Slf4j
@Validated
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Validated(Create.class) @RequestBody UserDto dto) {
        log.info("User API create: email={}", dto.getEmail());
        UserDto created = userService.createUser(dto);
        URI location = URI.create("/users/" + created.getId());
        log.info("User API created: id={} email={}", created.getId(), created.getEmail());
        return ResponseEntity.created(location).body(created);
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable Long id, @Validated(Update.class) @RequestBody UserDto dto) {
        log.info("User API update: id={} email={} name={}", id, dto.getEmail(), dto.getName());
        return userService.updateUser(id, dto);
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        log.debug("User API get by id: {}", id);
        return userService.getUserById(id);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.debug("User API list all");
        return userService.getAllUsers();
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        log.info("User API delete: {}", id);
        userService.deleteUser(id);
    }
}
