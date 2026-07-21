package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.item.dto.ItemShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestResponseDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeItemRequestResponseDto() throws JsonProcessingException {
        ItemShortDto itemDto = ItemShortDto.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .requestId(1L)
                .build();

        ItemRequestResponseDto responseDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(LocalDateTime.of(2023, 12, 1, 10, 0))
                .items(List.of(itemDto))
                .build();

        String json = objectMapper.writeValueAsString(responseDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"description\":\"Need a drill\"");
        assertThat(json).contains("\"created\":\"2023-12-01T10:00:00\"");
        assertThat(json).contains("\"items\":");
        assertThat(json).contains("\"name\":\"Drill\"");
        assertThat(json).contains("\"description\":\"Powerful drill\"");
        assertThat(json).contains("\"available\":true");
        assertThat(json).contains("\"requestId\":1");
    }

    @Test
    void shouldDeserializeItemRequestResponseDto() throws JsonProcessingException {
        String json = "{\"id\":1,\"description\":\"Need a drill\",\"created\":\"2023-12-01T10:00:00\",\"items\":[]}";

        ItemRequestResponseDto responseDto = objectMapper.readValue(json, ItemRequestResponseDto.class);

        assertThat(responseDto.getId()).isEqualTo(1L);
        assertThat(responseDto.getDescription()).isEqualTo("Need a drill");
        assertThat(responseDto.getCreated()).isEqualTo(LocalDateTime.of(2023, 12, 1, 10, 0));
        assertThat(responseDto.getItems()).isEmpty();
    }

    @Test
    void shouldDeserializeItemRequestResponseDtoWithItems() throws JsonProcessingException {
        String json = "{\"id\":1,\"description\":\"Need a drill\",\"created\":\"2023-12-01T10:00:00\",\"items\":[{\"id\":1,\"name\":\"Drill\",\"description\":\"Powerful drill\",\"available\":true,\"requestId\":1}]}";

        ItemRequestResponseDto responseDto = objectMapper.readValue(json, ItemRequestResponseDto.class);

        assertThat(responseDto.getId()).isEqualTo(1L);
        assertThat(responseDto.getDescription()).isEqualTo("Need a drill");
        assertThat(responseDto.getCreated()).isEqualTo(LocalDateTime.of(2023, 12, 1, 10, 0));
        assertThat(responseDto.getItems()).hasSize(1);
        assertThat(responseDto.getItems().get(0).getId()).isEqualTo(1L);
        assertThat(responseDto.getItems().get(0).getName()).isEqualTo("Drill");
        assertThat(responseDto.getItems().get(0).getDescription()).isEqualTo("Powerful drill");
        assertThat(responseDto.getItems().get(0).getAvailable()).isTrue();
        assertThat(responseDto.getItems().get(0).getRequestId()).isEqualTo(1L);
    }
}