package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_ShouldCreateUser() {
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setName("Test User");
        userCreateDto.setEmail("test@mail.com");

        User user = new User();
        user.setName("Test User");
        user.setEmail("test@mail.com");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Test User");
        savedUser.setEmail("test@mail.com");

        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Test User");
        userDto.setEmail("test@mail.com");

        when(userRepository.existsByEmail("test@mail.com")).thenReturn(false);
        when(userMapper.toUser(any(UserCreateDto.class))).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toUserDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.createUser(userCreateDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test User", result.getName());
        verify(userRepository).existsByEmail("test@mail.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WhenEmailExists_ShouldThrowException() {
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setName("Test User");
        userCreateDto.setEmail("existing@mail.com");

        when(userRepository.existsByEmail("existing@mail.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.createUser(userCreateDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUser_ShouldReturnUser() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setName("Test User");

        UserDto userDto = new UserDto();
        userDto.setId(userId);
        userDto.setName("Test User");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.getUser(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Test User", result.getName());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUser_WhenUserNotExists_ShouldThrowException() {
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> userService.getUser(userId));
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("User 1");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("User 2");

        UserDto userDto1 = new UserDto();
        userDto1.setId(1L);
        userDto1.setName("User 1");

        UserDto userDto2 = new UserDto();
        userDto2.setId(2L);
        userDto2.setName("User 2");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(userMapper.toUserDto(user1)).thenReturn(userDto1);
        when(userMapper.toUserDto(user2)).thenReturn(userDto2);

        List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("User 1", result.get(0).getName());
        assertEquals("User 2", result.get(1).getName());
        verify(userRepository).findAll();
    }

    @Test
    void updateUser_ShouldUpdateUser() {
        Long userId = 1L;
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setName("Updated User");
        userUpdateDto.setEmail("updated@mail.com");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Old User");
        existingUser.setEmail("old@mail.com");

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName("Updated User");
        updatedUser.setEmail("updated@mail.com");

        UserDto userDto = new UserDto();
        userDto.setId(userId);
        userDto.setName("Updated User");
        userDto.setEmail("updated@mail.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailAndIdNot("updated@mail.com", userId)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toUserDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.updateUser(userId, userUpdateDto);

        assertNotNull(result);
        assertEquals("Updated User", result.getName());
        assertEquals("updated@mail.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WhenEmailExistsForOtherUser_ShouldThrowException() {
        Long userId = 1L;
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setEmail("existing@mail.com");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("old@mail.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailAndIdNot("existing@mail.com", userId)).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.updateUser(userId, userUpdateDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WhenOnlyNameProvided_ShouldUpdateOnlyName() {
        Long userId = 1L;
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setName("Updated User");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Old User");
        existingUser.setEmail("old@mail.com");

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName("Updated User");
        updatedUser.setEmail("old@mail.com");

        UserDto userDto = new UserDto();
        userDto.setId(userId);
        userDto.setName("Updated User");
        userDto.setEmail("old@mail.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toUserDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.updateUser(userId, userUpdateDto);

        assertNotNull(result);
        assertEquals("Updated User", result.getName());
        assertEquals("old@mail.com", result.getEmail());
    }

    @Test
    void deleteUser_ShouldDeleteUser() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        userService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_WhenUserNotExists_ShouldThrowException() {
        Long userId = 999L;

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(java.util.NoSuchElementException.class, () -> userService.deleteUser(userId));
    }
}