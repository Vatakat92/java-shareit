package ru.practicum.shareit.item.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.testutil.EntityBuilders;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentServiceImpl Unit Tests")
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User user;
    private Item item;
    private Booking pastApprovedBooking;
    private CommentDto commentDto;
    private Comment savedComment;

    @BeforeEach
    void setUp() {
        user = EntityBuilders.user()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        item = EntityBuilders.item()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .owner(user)
                .build();

        pastApprovedBooking = EntityBuilders.booking()
                .id(1L)
                .start(LocalDateTime.now().minusDays(3))
                .end(LocalDateTime.now().minusDays(2))
                .status(BookingStatus.APPROVED)
                .booker(user)
                .item(item)
                .build();

        commentDto = new CommentDto();
        commentDto.setText("This is a great item!");

        savedComment = EntityBuilders.comment()
                .id(1L)
                .text("This is a great item!")
                .created(LocalDateTime.now())
                .author(user)
                .item(item)
                .build();
    }

    @Test
    @DisplayName("Should create comment successfully when user has past approved booking")
    void createComment_WhenUserHasPastApprovedBooking_ShouldCreateComment() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findPastApprovedForComment(eq(1L), eq(1L), any(LocalDateTime.class)))
                .thenReturn(Optional.of(pastApprovedBooking));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentDto result = commentService.createComment(1L, 1L, commentDto);

        assertNotNull(result);
        assertEquals("This is a great item!", result.getText());
        assertNotNull(result.getCreated());
        assertEquals("Test User", result.getAuthorName());

        verify(userRepository).findById(1L);
        verify(itemRepository).findById(1L);
        verify(bookingRepository).findPastApprovedForComment(eq(1L), eq(1L), any(LocalDateTime.class));
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user not found")
    void createComment_WhenUserNotFound_ShouldThrowEntityNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> commentService.createComment(1L, 1L, commentDto)
        );

        assertEquals("User not found: 1", exception.getMessage());

        verify(userRepository).findById(1L);
        verifyNoInteractions(itemRepository);
        verifyNoInteractions(bookingRepository);
        verifyNoInteractions(commentRepository);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when item not found")
    void createComment_WhenItemNotFound_ShouldThrowEntityNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> commentService.createComment(1L, 1L, commentDto)
        );

        assertEquals("Item not found: 1", exception.getMessage());

        verify(userRepository).findById(1L);
        verify(itemRepository).findById(1L);
        verifyNoInteractions(bookingRepository);
        verifyNoInteractions(commentRepository);
    }

    @Test
    @DisplayName("Should throw ValidationException when user has no past approved booking")
    void createComment_WhenUserHasNoPastApprovedBooking_ShouldThrowValidationException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findPastApprovedForComment(eq(1L), eq(1L), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> commentService.createComment(1L, 1L, commentDto)
        );

        assertEquals("User must have an approved past booking to comment", exception.getMessage());

        verify(userRepository).findById(1L);
        verify(itemRepository).findById(1L);
        verify(bookingRepository).findPastApprovedForComment(eq(1L), eq(1L), any(LocalDateTime.class));
        verifyNoInteractions(commentRepository);
    }

    @Test
    @DisplayName("Should throw ValidationException when user has future booking")
    void createComment_WhenUserHasFutureBooking_ShouldThrowValidationException() {
        Booking futureBooking = EntityBuilders.booking()
                .id(2L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.APPROVED)
                .booker(user)
                .item(item)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findPastApprovedForComment(eq(1L), eq(1L), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> commentService.createComment(1L, 1L, commentDto)
        );

        assertEquals("User must have an approved past booking to comment", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw ValidationException when user has pending booking")
    void createComment_WhenUserHasPendingBooking_ShouldThrowValidationException() {
        Booking pendingBooking = EntityBuilders.booking()
                .id(3L)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.WAITING)
                .booker(user)
                .item(item)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findPastApprovedForComment(eq(1L), eq(1L), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> commentService.createComment(1L, 1L, commentDto)
        );

        assertEquals("User must have an approved past booking to comment", exception.getMessage());
    }
}
