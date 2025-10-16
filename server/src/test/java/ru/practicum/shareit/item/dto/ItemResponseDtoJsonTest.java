package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemResponseDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeItemResponseDto() throws JsonProcessingException {
        ItemResponseDto responseDto = new ItemResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Test Item");
        responseDto.setDescription("Test Description");
        responseDto.setAvailable(true);
        responseDto.setOwnerId(1L);

        String json = objectMapper.writeValueAsString(responseDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Test Item\"");
        assertThat(json).contains("\"description\":\"Test Description\"");
        assertThat(json).contains("\"available\":true");
        assertThat(json).contains("\"ownerId\":1");
    }

    @Test
    void shouldDeserializeItemResponseDto() throws JsonProcessingException {
        String json = "{\"id\":1,\"name\":\"Test Item\",\"description\":\"Test Description\",\"available\":true,\"ownerId\":1}";

        ItemResponseDto responseDto = objectMapper.readValue(json, ItemResponseDto.class);

        assertThat(responseDto.getId()).isEqualTo(1L);
        assertThat(responseDto.getName()).isEqualTo("Test Item");
        assertThat(responseDto.getDescription()).isEqualTo("Test Description");
        assertThat(responseDto.getAvailable()).isTrue();
        assertThat(responseDto.getOwnerId()).isEqualTo(1L);
    }
}