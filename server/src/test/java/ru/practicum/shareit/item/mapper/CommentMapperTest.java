package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentMapperTest {

    @Test
    void toDto_ShouldMapCommentToCommentDto() {
        User author = new User();
        author.setId(1L);
        author.setName("Test User");

        Item item = new Item();
        item.setId(1L);

        ru.practicum.shareit.item.model.Comment comment = new ru.practicum.shareit.item.model.Comment();
        comment.setId(1L);
        comment.setText("Test comment");
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());

        CommentDto result = CommentMapper.toDto(comment);

        assertNotNull(result);
        assertEquals(comment.getId(), result.getId());
        assertEquals(comment.getText(), result.getText());
        assertEquals(item.getId(), result.getItemId());
        assertEquals(author.getId(), result.getAuthorId());
        assertEquals(author.getName(), result.getAuthorName());
        assertEquals(comment.getCreated(), result.getCreated());
    }

    @Test
    void toEntity_ShouldMapCommentCreateDtoToComment() {
        CommentCreateDto dto = new CommentCreateDto();
        dto.setText("Test comment");

        User author = new User();
        author.setId(1L);

        Item item = new Item();
        item.setId(1L);

        ru.practicum.shareit.item.model.Comment result = CommentMapper.toEntity(dto, item, author);

        assertNotNull(result);
        assertEquals(dto.getText(), result.getText());
        assertEquals(author, result.getAuthor());
        assertEquals(item, result.getItem());
    }
}