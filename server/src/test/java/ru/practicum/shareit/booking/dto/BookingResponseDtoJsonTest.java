package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserShortDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingResponseDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeBookingResponseDto() throws Exception {
        ItemShortDto itemDto = new ItemShortDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");

        UserShortDto userDto = new UserShortDto();
        userDto.setId(1L);
        userDto.setName("Test User");

        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(1L);
        responseDto.setStart(LocalDateTime.of(2023, 12, 1, 10, 0));
        responseDto.setEnd(LocalDateTime.of(2023, 12, 2, 10, 0));
        responseDto.setStatus(BookingStatus.WAITING);
        responseDto.setItem(itemDto);
        responseDto.setBooker(userDto);

        String json = objectMapper.writeValueAsString(responseDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"status\":\"WAITING\"");
        assertThat(json).contains("\"start\":\"2023-12-01T10:00:00\"");
        assertThat(json).contains("\"end\":\"2023-12-02T10:00:00\"");
        assertThat(json).contains("\"item\"");
        assertThat(json).contains("\"booker\"");
    }

    @Test
    void shouldDeserializeBookingResponseDto() throws Exception {
        String json = "{" +
                "\"id\":1," +
                "\"start\":\"2023-12-01T10:00:00\"," +
                "\"end\":\"2023-12-02T10:00:00\"," +
                "\"status\":\"APPROVED\"," +
                "\"item\":{\"id\":1,\"name\":\"Test Item\"}," +
                "\"booker\":{\"id\":1,\"name\":\"Test User\"}" +
                "}";

        BookingResponseDto responseDto = objectMapper.readValue(json, BookingResponseDto.class);

        assertThat(responseDto.getId()).isEqualTo(1L);
        assertThat(responseDto.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(responseDto.getStart()).isEqualTo(LocalDateTime.of(2023, 12, 1, 10, 0));
        assertThat(responseDto.getEnd()).isEqualTo(LocalDateTime.of(2023, 12, 2, 10, 0));
        assertThat(responseDto.getItem()).isNotNull();
        assertThat(responseDto.getItem().getId()).isEqualTo(1L);
        assertThat(responseDto.getBooker()).isNotNull();
        assertThat(responseDto.getBooker().getId()).isEqualTo(1L);
    }
}