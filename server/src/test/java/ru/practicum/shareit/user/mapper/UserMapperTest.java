package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    void toUser_ShouldMapUserCreateDtoToUser() {
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setName("Test User");
        userCreateDto.setEmail("test@mail.com");

        User result = userMapper.toUser(userCreateDto);

        assertNotNull(result);
        assertEquals("Test User", result.getName());
        assertEquals("test@mail.com", result.getEmail());
    }

    @Test
    void toUserDto_ShouldMapUserToUserDto() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@mail.com");

        UserDto result = userMapper.toUserDto(user);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@mail.com", result.getEmail());
    }

    @Test
    void updateUserFromDto_ShouldUpdateUserFields() {
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setName("Updated Name");
        userUpdateDto.setEmail("updated@mail.com");

        User user = new User();
        user.setId(1L);
        user.setName("Old Name");
        user.setEmail("old@mail.com");

        userMapper.updateUserFromDto(userUpdateDto, user);

        assertEquals("Updated Name", user.getName());
        assertEquals("updated@mail.com", user.getEmail());
    }

    @Test
    void updateUserFromDto_WhenFieldsAreNull_ShouldNotUpdate() {
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setName(null);
        userUpdateDto.setEmail(null);

        User user = new User();
        user.setId(1L);
        user.setName("Old Name");
        user.setEmail("old@mail.com");

        userMapper.updateUserFromDto(userUpdateDto, user);

        assertEquals("Old Name", user.getName());
        assertEquals("old@mail.com", user.getEmail());
    }
}