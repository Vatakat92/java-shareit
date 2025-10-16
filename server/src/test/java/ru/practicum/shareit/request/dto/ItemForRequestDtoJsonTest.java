package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemForRequestDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeItemForRequestDto() throws JsonProcessingException {
        ItemForRequestDto itemDto = ItemForRequestDto.builder()
                .id(1L)
                .name("Drill")
                .ownerId(1L)
                .build();

        String json = objectMapper.writeValueAsString(itemDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Drill\"");
        assertThat(json).contains("\"ownerId\":1");
    }

    @Test
    void shouldDeserializeItemForRequestDto() throws JsonProcessingException {
        String json = "{\"id\":1,\"name\":\"Drill\",\"ownerId\":1}";

        ItemForRequestDto itemDto = objectMapper.readValue(json, ItemForRequestDto.class);

        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getName()).isEqualTo("Drill");
        assertThat(itemDto.getOwnerId()).isEqualTo(1L);
    }
}