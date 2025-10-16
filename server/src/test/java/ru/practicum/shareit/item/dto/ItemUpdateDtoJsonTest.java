package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemUpdateDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeItemUpdateDto() throws JsonProcessingException {
        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("Updated Name");
        updateDto.setDescription("Updated Description");
        updateDto.setAvailable(true);

        String json = objectMapper.writeValueAsString(updateDto);

        assertThat(json).contains("\"name\":\"Updated Name\"");
        assertThat(json).contains("\"description\":\"Updated Description\"");
        assertThat(json).contains("\"available\":true");
    }

    @Test
    void shouldDeserializeItemUpdateDto() throws JsonProcessingException {
        String json = "{\"name\":\"Updated Name\",\"description\":\"Updated Description\",\"available\":true}";

        ItemUpdateDto updateDto = objectMapper.readValue(json, ItemUpdateDto.class);

        assertThat(updateDto.getName()).isEqualTo("Updated Name");
        assertThat(updateDto.getDescription()).isEqualTo("Updated Description");
        assertThat(updateDto.getAvailable()).isTrue();
    }

    @Test
    void shouldHandleNullValuesInItemUpdateDto() throws JsonProcessingException {
        String json = "{}";

        ItemUpdateDto updateDto = objectMapper.readValue(json, ItemUpdateDto.class);

        assertThat(updateDto.getName()).isNull();
        assertThat(updateDto.getDescription()).isNull();
        assertThat(updateDto.getAvailable()).isNull();
    }
}