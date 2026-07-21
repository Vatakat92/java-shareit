package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingCreateDtoJson1TestGateway {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeBookingDto() throws JsonProcessingException {
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setItemId(1L);
        LocalDateTime start = LocalDateTime.of(2023, 12, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2023, 12, 2, 10, 0);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);

        String json = objectMapper.writeValueAsString(bookingDto);

        assertThat(json).contains("\"itemId\":1");
        assertThat(json).contains("\"start\":\"2023-12-01T10:00:00\"");
        assertThat(json).contains("\"end\":\"2023-12-02T10:00:00\"");
    }

    @Test
    void shouldDeserializeBookingDto() throws JsonProcessingException {
        String json = "{\"itemId\":1,\"start\":\"2023-12-01T10:00:00\",\"end\":\"2023-12-02T10:00:00\"}";

        BookingCreateDto bookingDto = objectMapper.readValue(json, BookingCreateDto.class);

        assertThat(bookingDto.getItemId()).isEqualTo(1L);
        assertThat(bookingDto.getStart()).isEqualTo(LocalDateTime.of(2023, 12,
                1, 10, 0));
        assertThat(bookingDto.getEnd()).isEqualTo(LocalDateTime.of(2023, 12,
                2, 10, 0));
    }

    @Test
    void shouldHandleNullValues() throws JsonProcessingException {
        BookingCreateDto bookingDto = new BookingCreateDto();

        String json = objectMapper.writeValueAsString(bookingDto);

        assertThat(json).contains("\"itemId\":null");
        assertThat(json).contains("\"start\":null");
        assertThat(json).contains("\"end\":null");
    }
}