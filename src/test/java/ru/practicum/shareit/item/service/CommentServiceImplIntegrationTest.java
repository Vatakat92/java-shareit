package ru.practicum.shareit.item.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.testutil.EntityBuilders;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("CommentServiceImpl Integration Tests")
class CommentServiceImplIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User user;
    private User owner;
    private Item item;
    private Booking pastApprovedBooking;

    @BeforeEach
    void setUp() {
        user = EntityBuilders.user()
                .name("Comment User")
                .email("comment@example.com")
                .build();
        user = userRepository.save(user);

        owner = EntityBuilders.user()
                .name("Item Owner")
                .email("owner@example.com")
                .build();
        owner = userRepository.save(owner);

        item = EntityBuilders.item()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .owner(owner)
                .build();
        item = itemRepository.save(item);

        pastApprovedBooking = EntityBuilders.booking()
                .startDate(LocalDateTime.now().minusDays(5))
                .endDate(LocalDateTime.now().minusDays(3))
                .status(BookingStatus.APPROVED)
                .booker(user)
                .item(item)
                .build();
        pastApprovedBooking = bookingRepository.save(pastApprovedBooking);
    }

    @Test
    @DisplayName("Should create comment successfully with valid booking")
    void createComment_WithValidPastApprovedBooking_ShouldCreateComment() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("This is a great item! I really enjoyed using it.");

        CommentDto result = commentService.createComment(user.getId(), item.getId(), commentDto);

        assertNotNull(result);
        assertEquals("This is a great item! I really enjoyed using it.", result.getText());
        assertNotNull(result.getCreated());
        assertEquals("Comment User", result.getAuthorName());
        assertNotNull(result.getId());
    }

    @Test
    @DisplayName("Should throw ValidationException when user has no past bookings")
    void createComment_WithoutPastApprovedBooking_ShouldThrowValidationException() {
        User userWithoutBooking = userRepository.save(EntityBuilders.user()
                .name("User Without Booking")
                .email("nobooking@example.com")
                .build());

        CommentDto commentDto = new CommentDto();
        commentDto.setText("This should fail - no booking history");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> commentService.createComment(userWithoutBooking.getId(), item.getId(), commentDto)
        );

        assertEquals("Вы не можете оставить отзыв, так как не бронировали эту вещь", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw ValidationException when user has future booking")
    void createComment_WithFutureBooking_ShouldThrowValidationException() {
        User userWithFutureBooking = userRepository.save(EntityBuilders.user()
                .name("User With Future Booking")
                .email("future@example.com")
                .build());

        Booking futureBooking = EntityBuilders.booking()
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.APPROVED)
                .booker(userWithFutureBooking)
                .item(item)
                .build();
        bookingRepository.save(futureBooking);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("This should fail - future booking not completed");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> commentService.createComment(userWithFutureBooking.getId(), item.getId(), commentDto)
        );

        assertEquals("Вы не можете оставить отзыв, так как не бронировали эту вещь", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw ValidationException when user has pending booking")
    void createComment_WithPendingBooking_ShouldThrowValidationException() {
        User userWithPendingBooking = userRepository.save(EntityBuilders.user()
                .name("User With Pending Booking")
                .email("pending@example.com")
                .build());

        Booking pendingBooking = EntityBuilders.booking()
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.WAITING)
                .booker(userWithPendingBooking)
                .item(item)
                .build();
        bookingRepository.save(pendingBooking);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("This should fail");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> commentService.createComment(userWithPendingBooking.getId(), item.getId(), commentDto)
        );

        assertEquals("Вы не можете оставить отзыв, так как не бронировали эту вещь", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user not found")
    void createComment_WithNonExistentUser_ShouldThrowEntityNotFoundException() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("This should fail");

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> commentService.createComment(999L, item.getId(), commentDto)
        );

        assertEquals("Пользователь не найден: 999", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when item not found")
    void createComment_WithNonExistentItem_ShouldThrowEntityNotFoundException() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("This should fail");

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> commentService.createComment(user.getId(), 999L, commentDto)
        );

        assertEquals("Вещь не найдена: 999", exception.getMessage());
    }

    @Test
    @DisplayName("Should create multiple comments for the same item")
    void createComment_MultipleCommentsForSameItem_ShouldCreateAll() {
        CommentDto commentDto1 = new CommentDto();
        commentDto1.setText("First comment");

        CommentDto commentDto2 = new CommentDto();
        commentDto2.setText("Second comment");

        CommentDto result1 = commentService.createComment(user.getId(), item.getId(), commentDto1);
        CommentDto result2 = commentService.createComment(user.getId(), item.getId(), commentDto2);

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals("First comment", result1.getText());
        assertEquals("Second comment", result2.getText());
        assertNotEquals(result1.getId(), result2.getId());
    }
}
