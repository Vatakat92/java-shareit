package ru.practicum.shareit.testutil;

import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

/**
 * Utility class for creating test data builders
 */
public class TestDataBuilder {

    public static User.UserBuilder defaultUser() {
        return User.builder()
                .name("Test User")
                .email("test@example.com");
    }

    public static LocalDateTime pastDateTime(int daysAgo) {
        return LocalDateTime.now().minusDays(daysAgo);
    }

    public static LocalDateTime futureDateTime(int daysAhead) {
        return LocalDateTime.now().plusDays(daysAhead);
    }
}
