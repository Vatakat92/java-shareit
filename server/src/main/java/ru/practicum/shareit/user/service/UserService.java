package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(UserCreateDto userDto);

    UserDto updateUser(Long userId, UserUpdateDto userDto);

    UserDto getUser(Long userId);

    List<UserDto> getAllUsers();

    void deleteUser(Long userId);
}