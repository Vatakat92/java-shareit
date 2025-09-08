package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.InMemoryUserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceImplTest {
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(new InMemoryUserRepository());
    }

    @Test
    void createUser_ok() {
        UserDto created = service.createUser(new UserDto(null, "Alice", "alice@mail.com"));
        assertNotNull(created.getId());
        assertEquals("Alice", created.getName());
        assertEquals("alice@mail.com", created.getEmail());
    }

    @Test
    void createUser_duplicateEmail_conflict() {
        service.createUser(new UserDto(null, "Alice", "dup@mail.com"));
        ConflictException ex = assertThrows(ConflictException.class,
                () -> service.createUser(new UserDto(null, "Bob", "dup@mail.com")));
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
    }

    @Test
    void getUser_notFound() {
        assertThrows(NoSuchElementException.class, () -> service.getUserById(999L));
    }

    @Test
    void updateUser_emailUniqueness() {
        service.createUser(new UserDto(null, "A", "a@mail.com"));
        long userId = service.createUser(new UserDto(null, "B", "b@mail.com")).getId();
        assertThrows(ConflictException.class, () -> service.updateUser(userId,
                new UserDto(null, null, "a@mail.com")));
    }

    @Test
    void deleteUser_notFound() {
        assertThrows(NoSuchElementException.class, () -> service.deleteUser(123L));
    }

    @Test
    void getAllUsers_sortedByIdAsc() {
        service.createUser(new UserDto(null, "B", "b@mail.com"));
        service.createUser(new UserDto(null, "A", "a@mail.com"));
        service.createUser(new UserDto(null, "C", "c@mail.com"));

        var list = service.getAllUsers();
        assertEquals(3, list.size());
        assertTrue(list.get(0).getId() < list.get(1).getId() && list.get(1).getId() < list.get(2).getId());
    }
}
