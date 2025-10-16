package ru.practicum.shareit.user.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@Component
@Slf4j
public class UserMapper {
    public UserDto toUserDto(User user) {
        if (user == null) {
            log.error("User is null in toUserDto");
            throw new IllegalArgumentException("User cannot be null");
        }
        log.debug("Mapping User to UserDto: id={}", user.getId());
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public User toUser(UserCreateDto userDto) {
        if (userDto == null) {
            log.error("UserDto is null in toUser");
            throw new IllegalArgumentException("UserDto cannot be null");
        }
        log.debug("Mapping UserDto to User: email={}", userDto.getEmail());
        return User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public void updateUserFromDto(UserUpdateDto userDto, User user) {
        if (userDto == null || user == null) {
            return;
        }
        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            user.setEmail(userDto.getEmail());
        }
    }
}