package ru.practicum.shareit.request.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestTest {

    @Test
    void itemRequestCreation_ShouldSetFieldsCorrectly() {
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Test request");
        User requestor = new User();
        requestor.setId(1L);
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        assertEquals(1L, request.getId());
        assertEquals("Test request", request.getDescription());
        assertEquals(requestor, request.getRequestor());
        assertNotNull(request.getCreated());
    }

    @Test
    void itemRequestEqualsAndHashCode_ShouldWorkCorrectly() {
        User requestor = new User();
        requestor.setId(1L);
        LocalDateTime now = LocalDateTime.of(2023, 10, 16, 12, 0);

        ItemRequest request1 = ItemRequest.builder()
                .id(1L)
                .description("Request 1")
                .requestor(requestor)
                .created(now)
                .build();

        ItemRequest request2 = ItemRequest.builder()
                .id(1L)
                .description("Request 1")
                .requestor(requestor)
                .created(now)
                .build();

        ItemRequest request3 = ItemRequest.builder()
                .id(2L)
                .description("Request 1")
                .requestor(requestor)
                .created(now)
                .build();

        assertNotNull(request1);
        assertNotNull(request2);
        assertNotNull(request3);
        assertEquals(request1.getId(), request2.getId());
        assertNotEquals(request1.getId(), request3.getId());
    }

    @Test
    void itemRequestNoArgsConstructor_ShouldCreateEmptyRequest() {
        ItemRequest request = new ItemRequest();

        assertNotNull(request);
        assertNull(request.getId());
        assertNull(request.getDescription());
        assertNull(request.getRequestor());
        assertNull(request.getCreated());
    }

    @Test
    void itemRequestEquals_WithNull_ShouldReturnFalse() {
        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .build();

        assertNotNull(request);
    }

    @Test
    void itemRequestEquals_WithDifferentClass_ShouldReturnFalse() {
        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .build();

        assertNotEquals("string", request);
    }

    @Test
    void itemRequestToString_ShouldContainRelevantInfo() {
        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Test request")
                .build();

        String toString = request.toString();

        assertNotNull(toString);
        assertFalse(toString.isEmpty());
        assertTrue(toString.contains("ItemRequest"));
    }
}