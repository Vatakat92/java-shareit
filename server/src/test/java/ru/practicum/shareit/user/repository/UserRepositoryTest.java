package ru.practicum.shareit.user.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

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
    void save_ShouldSaveUser() {
        User newUser = new User();
        newUser.setName("New User");
        newUser.setEmail("new@mail.com");

        User saved = userRepository.save(newUser);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("New User", saved.getName());
        assertEquals("new@mail.com", saved.getEmail());
    }

    @Test
    void findById_ShouldReturnUser() {
        Optional<User> result = userRepository.findById(testUser.getId());

        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        assertEquals("Test User", result.get().getName());
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        User secondUser = new User();
        secondUser.setName("Second User");
        secondUser.setEmail("second@mail.com");
        userRepository.save(secondUser);

        List<User> result = userRepository.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void deleteById_ShouldDeleteUser() {
        userRepository.deleteById(testUser.getId());

        Optional<User> deleted = userRepository.findById(testUser.getId());
        assertFalse(deleted.isPresent());
    }

    @Test
    void existsByEmail_WhenEmailExists_ShouldReturnTrue() {
        boolean result = userRepository.existsByEmail("test@mail.com");

        assertTrue(result);
    }

    @Test
    void existsByEmail_WhenEmailNotExists_ShouldReturnFalse() {
        boolean result = userRepository.existsByEmail("nonexistent@mail.com");

        assertFalse(result);
    }

    @Test
    void existsByEmailAndIdNot_WhenEmailExistsForOtherUser_ShouldReturnTrue() {
        User secondUser = new User();
        secondUser.setName("Second User");
        secondUser.setEmail("second@mail.com");
        userRepository.save(secondUser);

        boolean result = userRepository.existsByEmailAndIdNot("second@mail.com", testUser.getId());

        assertTrue(result);
    }

    @Test
    void existsByEmailAndIdNot_WhenEmailExistsForSameUser_ShouldReturnFalse() {
        boolean result = userRepository.existsByEmailAndIdNot("test@mail.com", testUser.getId());

        assertFalse(result);
    }

    @Test
    void existsByEmailAndIdNot_WhenEmailNotExists_ShouldReturnFalse() {
        boolean result = userRepository.existsByEmailAndIdNot("nonexistent@mail.com", testUser.getId());

        assertFalse(result);
    }
}