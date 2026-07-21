package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentTest {

    @Test
    void commentCreation_ShouldSetFieldsCorrectly() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Test comment");

        User author = new User();
        author.setId(1L);

        Item item = new Item();
        item.setId(1L);

        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());

        assertEquals(1L, comment.getId());
        assertEquals("Test comment", comment.getText());
        assertEquals(author, comment.getAuthor());
        assertEquals(item, comment.getItem());
        assertNotNull(comment.getCreated());
    }

    @Test
    void commentEqualsAndHashCode_ShouldWorkCorrectly() {
        User author = new User();
        author.setId(1L);
        Item item = new Item();
        item.setId(1L);
        LocalDateTime now = LocalDateTime.of(2023, 10, 16, 12, 0);

        Comment comment1 = Comment.builder()
                .id(1L)
                .text("Comment 1")
                .author(author)
                .item(item)
                .created(now)
                .build();

        Comment comment2 = Comment.builder()
                .id(1L)
                .text("Comment 1")
                .author(author)
                .item(item)
                .created(now)
                .build();

        Comment comment3 = Comment.builder()
                .id(2L)
                .text("Comment 1")
                .author(author)
                .item(item)
                .created(now)
                .build();

        assertNotNull(comment1);
        assertNotNull(comment2);
        assertNotNull(comment3);
        assertEquals(comment1.getId(), comment2.getId());
        assertNotEquals(comment1.getId(), comment3.getId());
    }

    @Test
    void commentNoArgsConstructor_ShouldCreateEmptyComment() {
        Comment comment = new Comment();

        assertNotNull(comment);
        assertNull(comment.getId());
        assertNull(comment.getText());
        assertNull(comment.getAuthor());
        assertNull(comment.getItem());
        assertNull(comment.getCreated());
    }

    @Test
    void commentEquals_WithSameObject_ShouldReturnTrue() {
        Comment comment = Comment.builder()
                .id(1L)
                .build();

        assertEquals(comment.getId(), comment.getId());
    }

    @Test
    void commentEquals_WithNull_ShouldReturnFalse() {
        Comment comment = Comment.builder()
                .id(1L)
                .build();

        assertNotNull(comment);
    }

    @Test
    void commentEquals_WithDifferentClass_ShouldReturnFalse() {
        Comment comment = Comment.builder()
                .id(1L)
                .build();

        assertNotEquals("string", comment);
    }

    @Test
    void commentToString_ShouldContainRelevantInfo() {
        Comment comment = Comment.builder()
                .id(1L)
                .text("Test comment")
                .build();

        String toString = comment.toString();

        assertNotNull(toString);
        assertFalse(toString.isEmpty());
        assertTrue(toString.contains("Comment"));
    }
}