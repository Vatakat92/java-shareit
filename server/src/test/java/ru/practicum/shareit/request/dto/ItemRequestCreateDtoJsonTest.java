package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestCreateDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeItemRequestCreateDto() throws JsonProcessingException {
        ItemRequestCreateDto requestDto = ItemRequestCreateDto.builder()
                .description("Need a drill")
                .build();

        String json = objectMapper.writeValueAsString(requestDto);

        assertThat(json).contains("\"description\":\"Need a drill\"");
    }

    @Test
    void shouldDeserializeItemRequestCreateDto() throws JsonProcessingException {
        String json = "{\"description\":\"Need a drill\"}";

        ItemRequestCreateDto requestDto = objectMapper.readValue(json, ItemRequestCreateDto.class);

        assertThat(requestDto.getDescription()).isEqualTo("Need a drill");
    }
}