package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemCreateDtoJsonTest {

    @Autowired

    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeItemCreateDto() throws JsonProcessingException {
        ItemCreateDto itemDto = new ItemCreateDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(10L);

        String json = objectMapper.writeValueAsString(itemDto);

        assertThat(json).contains("\"name\":\"Test Item\"");
        assertThat(json).contains("\"description\":\"Test Description\"");
        assertThat(json).contains("\"available\":true");
        assertThat(json).contains("\"requestId\":10");
    }

    @Test
    void shouldDeserializeItemCreateDto() throws JsonProcessingException {
        String json = "{\"name\":\"Test Item\",\"description\":\"Test Description\",\"available\":true,\"requestId\":10}";

        ItemCreateDto itemDto = objectMapper.readValue(json, ItemCreateDto.class);

        assertThat(itemDto.getName()).isEqualTo("Test Item");
        assertThat(itemDto.getDescription()).isEqualTo("Test Description");
        assertThat(itemDto.getAvailable()).isTrue();
        assertThat(itemDto.getRequestId()).isEqualTo(10L);
    }
}