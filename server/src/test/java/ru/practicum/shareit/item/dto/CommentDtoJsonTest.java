package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeCommentDto() throws JsonProcessingException {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Great item!");
        commentDto.setItemId(10L);
        commentDto.setAuthorId(5L);
        commentDto.setAuthorName("Test User");
        commentDto.setCreated(LocalDateTime.of(2023, 12, 1, 10, 0));

        String json = objectMapper.writeValueAsString(commentDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"text\":\"Great item!\"");
        assertThat(json).contains("\"itemId\":10");
        assertThat(json).contains("\"authorId\":5");
        assertThat(json).contains("\"authorName\":\"Test User\"");
        assertThat(json).contains("\"created\":\"2023-12-01T10:00:00\"");
    }

    @Test
    void shouldDeserializeCommentDto() throws JsonProcessingException {
        String json = "{\"id\":1,\"text\":\"Great item!\",\"itemId\":10,\"authorId\":5,\"authorName\":\"Test User\",\"created\":\"2023-12-01T10:00:00\"}";

        CommentDto commentDto = objectMapper.readValue(json, CommentDto.class);

        assertThat(commentDto.getId()).isEqualTo(1L);
        assertThat(commentDto.getText()).isEqualTo("Great item!");
        assertThat(commentDto.getItemId()).isEqualTo(10L);
        assertThat(commentDto.getAuthorId()).isEqualTo(5L);
        assertThat(commentDto.getAuthorName()).isEqualTo("Test User");
        assertThat(commentDto.getCreated()).isEqualTo(LocalDateTime.of(2023, 12, 1, 10, 0));
    }
}