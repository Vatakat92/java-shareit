package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.dto.ItemRequestInternalDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RequestMapperTest {

    @Test
    void toEntity_ShouldMapCorrectly() {
        ItemRequestInternalDto requestDto = ItemRequestInternalDto.builder()
                .id(1L)
                .description("Need a drill")
                .requestorId(1L)
                .created(LocalDateTime.of(2023, 12, 1, 10, 0))
                .build();

        User requestor = User.builder()
                .id(1L)
                .name("Requester")
                .build();

        ItemRequest result = ItemRequestMapper.toEntity(requestDto, requestor);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Need a drill", result.getDescription());
        assertEquals(requestor, result.getRequestor());
        assertEquals(requestDto.getCreated(), result.getCreated());
        assertTrue(result.getItems().isEmpty(), "Items list should be empty");
    }

    @Test
    void toEntity_WhenCreatedIsNull_ShouldSetCurrentTime() {
        ItemRequestInternalDto requestDto = ItemRequestInternalDto.builder()
                .id(1L)
                .description("Need a drill")
                .requestorId(1L)
                .created(null)
                .build();

        User requestor = User.builder()
                .id(1L)
                .name("Requester")
                .build();

        ItemRequest result = ItemRequestMapper.toEntity(requestDto, requestor);

        assertNotNull(result);
        assertEquals("Need a drill", result.getDescription());
        assertEquals(requestor, result.getRequestor());
        assertNotNull(result.getCreated(), "Created should be set to current time");
        assertTrue(result.getItems().isEmpty(), "Items list should be empty");
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void toEntity_WhenRequestDtoIsNull_ShouldReturnNull() {
        User requestor = User.builder()
                .id(1L)
                .build();

        ItemRequest result = ItemRequestMapper.toEntity(null, requestor);

        assertNull(result, "Result should be null when requestDto is null");
    }

    @Test
    void toResponseDto_ShouldMapCorrectly() {
        User requestor = User.builder()
                .id(1L)
                .build();

        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .description("Need a drill")
                .requestor(requestor)
                .created(LocalDateTime.of(2023, 12, 1, 10, 0))
                .items(java.util.List.of())
                .build();

        ItemRequestResponseDto result = ItemRequestMapper.toResponseDto(itemRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Need a drill", result.getDescription());
        assertEquals(itemRequest.getCreated(), result.getCreated());
        assertTrue(result.getItems().isEmpty(), "Items list should be empty");
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void toResponseDto_WhenRequestIsNull_ShouldReturnNull() {
        ItemRequestResponseDto result = ItemRequestMapper.toResponseDto(null);

        assertNull(result, "Result should be null when request is null");
    }
}