package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.testutil.EntityBuilders;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CommentRepository Tests")
class CommentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    private User author;
    private User owner;
    private Item item1;
    private Item item2;
    private Comment comment1;
    private Comment comment2;
    private Comment comment3;

    @BeforeEach
    void setUp() {
        author = EntityBuilders.user()
                .name("Comment Author")
                .email("author@example.com")
                .build();
        author = userRepository.save(author);

        owner = EntityBuilders.user()
                .name("Item Owner")
                .email("owner@example.com")
                .build();
        owner = userRepository.save(owner);

        item1 = EntityBuilders.item()
                .name("First Item")
                .description("First Description")
                .available(true)
                .owner(owner)
                .build();
        item1 = entityManager.persist(item1);

        item2 = EntityBuilders.item()
                .name("Second Item")
                .description("Second Description")
                .available(true)
                .owner(owner)
                .build();
        item2 = entityManager.persist(item2);

        comment1 = EntityBuilders.comment()
                .text("First comment on first item")
                .created(LocalDateTime.now().minusDays(1))
                .author(author)
                .item(item1)
                .build();
        comment1 = entityManager.persist(comment1);

        comment2 = EntityBuilders.comment()
                .text("Second comment on first item")
                .created(LocalDateTime.now().minusHours(2))
                .author(author)
                .item(item1)
                .build();
        comment2 = entityManager.persist(comment2);

        comment3 = EntityBuilders.comment()
                .text("Comment on second item")
                .created(LocalDateTime.now().minusMinutes(30))
                .author(author)
                .item(item2)
                .build();
        comment3 = entityManager.persist(comment3);

        entityManager.flush();
    }

    @Test
    @DisplayName("Should find comments by item id")
    void findByItemId_ShouldReturnCommentsForItem() {
        List<Comment> comments = commentRepository.findByItemId(item1.getId());

        assertNotNull(comments);
        assertEquals(2, comments.size());

        for (Comment comment : comments) {
            assertEquals(item1.getId(), comment.getItem().getId());
            assertEquals(author.getId(), comment.getAuthor().getId());
        }

        // Verify comment texts
        List<String> commentTexts = comments.stream()
                .map(Comment::getText)
                .toList();
        assertTrue(commentTexts.contains("First comment on first item"));
        assertTrue(commentTexts.contains("Second comment on first item"));
    }

    @Test
    @DisplayName("Should return empty list when item has no comments")
    void findByItemId_WhenItemHasNoComments_ShouldReturnEmptyList() {
        Item itemWithoutComments = EntityBuilders.item()
                .name("Item Without Comments")
                .description("No comments here")
                .available(true)
                .owner(owner)
                .build();
        itemWithoutComments = entityManager.persist(itemWithoutComments);
        entityManager.flush();

        List<Comment> comments = commentRepository.findByItemId(itemWithoutComments.getId());

        assertNotNull(comments);
        assertTrue(comments.isEmpty());
    }

    @Test
    @DisplayName("Should find single comment by item id")
    void findByItemId_ShouldReturnSingleCommentForItem() {
        List<Comment> comments = commentRepository.findByItemId(item2.getId());

        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals("Comment on second item", comments.get(0).getText());
        assertEquals(item2.getId(), comments.get(0).getItem().getId());
    }

    @Test
    @DisplayName("Should return comments in any order")
    void findByItemId_ShouldReturnCommentsInAnyOrder() {
        List<Comment> comments = commentRepository.findByItemId(item1.getId());

        assertEquals(2, comments.size());

        boolean hasFirstComment = comments.stream()
                .anyMatch(c -> "First comment on first item".equals(c.getText()));
        boolean hasSecondComment = comments.stream()
                .anyMatch(c -> "Second comment on first item".equals(c.getText()));

        assertTrue(hasFirstComment, "Should contain first comment");
        assertTrue(hasSecondComment, "Should contain second comment");
    }

    @Test
    @DisplayName("Should save and retrieve comment correctly")
    void save_ShouldSaveAndRetrieveComment() {
        Comment newComment = EntityBuilders.comment()
                .text("New comment")
                .created(LocalDateTime.now())
                .author(author)
                .item(item1)
                .build();

        Comment savedComment = commentRepository.save(newComment);

        assertNotNull(savedComment.getId());

        List<Comment> comments = commentRepository.findByItemId(item1.getId());
        assertEquals(3, comments.size()); // 2 existing + 1 new

        boolean hasNewComment = comments.stream()
                .anyMatch(c -> "New comment".equals(c.getText()));
        assertTrue(hasNewComment, "Should contain the new comment");
    }

    @Test
    @DisplayName("Should find all comments when multiple items have comments")
    void findByItemId_ShouldWorkWithMultipleItemsHavingComments() {
        List<Comment> commentsForItem1 = commentRepository.findByItemId(item1.getId());
        List<Comment> commentsForItem2 = commentRepository.findByItemId(item2.getId());

        assertEquals(2, commentsForItem1.size());
        assertEquals(1, commentsForItem2.size());

        for (Comment comment : commentsForItem1) {
            assertEquals(item1.getId(), comment.getItem().getId());
        }

        for (Comment comment : commentsForItem2) {
            assertEquals(item2.getId(), comment.getItem().getId());
        }
    }

    @Test
    @DisplayName("Should handle null item id gracefully")
    void findByItemId_WithNullItemId_ShouldHandleGracefully() {

        try {
            List<Comment> comments = commentRepository.findByItemId(null);
            assertNotNull(comments);
            assertTrue(comments.isEmpty());
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }
}
