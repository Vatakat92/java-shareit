package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestInternalDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeItemRequestInternalDto() throws JsonProcessingException {
        ItemRequestInternalDto requestDto = ItemRequestInternalDto.builder()
                .id(1L)
                .description("Need a drill")
                .requestorId(1L)
                .created(LocalDateTime.of(2023, 12, 1, 10, 0))
                .build();

        String json = objectMapper.writeValueAsString(requestDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"description\":\"Need a drill\"");
        assertThat(json).contains("\"requestorId\":1");
        assertThat(json).contains("\"created\":\"2023-12-01T10:00:00\"");
    }

    @Test
    void shouldDeserializeItemRequestInternalDto() throws JsonProcessingException {
        String json = "{\"id\":1,\"description\":\"Need a drill\",\"requestorId\":1,\"created\":\"2023-12-01T10:00:00\"}";

        ItemRequestInternalDto requestDto = objectMapper.readValue(json, ItemRequestInternalDto.class);

        assertThat(requestDto.getId()).isEqualTo(1L);
        assertThat(requestDto.getDescription()).isEqualTo("Need a drill");
        assertThat(requestDto.getRequestorId()).isEqualTo(1L);
        assertThat(requestDto.getCreated()).isEqualTo(LocalDateTime.of(2023, 12, 1, 10, 0));
    }
}