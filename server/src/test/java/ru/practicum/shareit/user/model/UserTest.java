package ru.practicum.shareit.user.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void userCreation_ShouldSetFieldsCorrectly() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@mail.com");

        assertEquals(1L, user.getId());
        assertEquals("Test User", user.getName());
        assertEquals("test@mail.com", user.getEmail());
    }

    @Test
    void userEqualsAndHashCode_ShouldWorkCorrectly() {
        User user1 = User.builder()
                .id(1L)
                .name("User 1")
                .email("user1@mail.com")
                .build();

        User user2 = User.builder()
                .id(1L)
                .name("User 1")
                .email("user1@mail.com")
                .build();

        User user3 = User.builder()
                .id(2L)
                .name("User 1")
                .email("user1@mail.com")
                .build();

        assertNotNull(user1);
        assertNotNull(user2);
        assertNotNull(user3);
        assertEquals(user1.getId(), user2.getId());
        assertNotEquals(user1.getId(), user3.getId());
    }

    @Test
    void userNoArgsConstructor_ShouldCreateEmptyObject() {
        User user = new User();

        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getName());
        assertNull(user.getEmail());
    }

    @Test
    void userEquals_WithNull_ShouldReturnFalse() {
        User user = User.builder()
                .id(1L)
                .build();

        assertNotNull(user);
    }

    @Test
    void userEquals_WithDifferentClass_ShouldReturnFalse() {
        User user = User.builder()
                .id(1L)
                .build();

        assertNotEquals("string", user);
    }

    @Test
    void userToString_ShouldContainRelevantInfo() {
        User user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@mail.com")
                .build();

        String toString = user.toString();

        assertNotNull(toString);
        assertFalse(toString.isEmpty());
        assertTrue(toString.contains("User"));
    }
}