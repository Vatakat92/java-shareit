package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@mail.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    void createUser_IntegrationTest() {
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setName("New User");
        userCreateDto.setEmail("newuser@mail.com");

        UserDto result = userService.createUser(userCreateDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("New User", result.getName());
        assertEquals("newuser@mail.com", result.getEmail());

        var savedUser = userRepository.findById(result.getId());
        assertTrue(savedUser.isPresent());
        assertEquals("New User", savedUser.get().getName());
    }

    @Test
    void createUser_WhenEmailExists_ShouldThrowException() {
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setName("Another User");
        userCreateDto.setEmail("test@mail.com"); // Same email as existing user

        assertThrows(ConflictException.class, () -> userService.createUser(userCreateDto));
    }

    @Test
    void getUser_IntegrationTest() {
        UserDto result = userService.getUser(testUser.getId());

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@mail.com", result.getEmail());
    }

    @Test
    void getUser_WhenUserNotExists_ShouldThrowException() {
        assertThrows(NoSuchElementException.class, () -> userService.getUser(999L));
    }

    @Test
    void getAllUsers_IntegrationTest() {
        User secondUser = new User();
        secondUser.setName("Second User");
        secondUser.setEmail("second@mail.com");
        userRepository.save(secondUser);

        List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getName().equals("Test User")));
        assertTrue(result.stream().anyMatch(u -> u.getName().equals("Second User")));
    }

    @Test
    void updateUser_IntegrationTest() {
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setName("Updated User");
        userUpdateDto.setEmail("updated@mail.com");

        UserDto result = userService.updateUser(testUser.getId(), userUpdateDto);

        assertNotNull(result);
        assertEquals("Updated User", result.getName());
        assertEquals("updated@mail.com", result.getEmail());

        var updatedUser = userRepository.findById(testUser.getId());
        assertTrue(updatedUser.isPresent());
        assertEquals("Updated User", updatedUser.get().getName());
    }

    @Test
    void updateUser_WhenEmailExistsForOtherUser_ShouldThrowException() {
        User secondUser = new User();
        secondUser.setName("Second User");
        secondUser.setEmail("second@mail.com");
        userRepository.save(secondUser);

        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setEmail("second@mail.com");

        assertThrows(ConflictException.class, () -> userService.updateUser(testUser.getId(), userUpdateDto));
    }

    @Test
    void deleteUser_IntegrationTest() {
        userService.deleteUser(testUser.getId());

        var deletedUser = userRepository.findById(testUser.getId());
        assertFalse(deletedUser.isPresent());
    }
}