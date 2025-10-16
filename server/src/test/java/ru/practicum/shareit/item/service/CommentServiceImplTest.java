package ru.practicum.shareit.item.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BusinessValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @Test
    void createComment_WhenValidData_ShouldCreateCommentAndReturnDto() {
        Long userId = 1L;
        Long itemId = 1L;
        CommentCreateDto commentDto = CommentCreateDto.builder()
                .text("Great item!")
                .build();

        User author = new User();
        author.setId(userId);
        author.setName("Test User");

        Item item = new Item();
        item.setId(itemId);

        Comment comment = Comment.builder()
                .id(1L)
                .text("Great item!")
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        CommentDto expectedResponse = CommentMapper.toDto(comment);

        when(userRepository.findById(userId)).thenReturn(Optional.of(author));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.existsCompletedBookingByUserAndItem(eq(userId), eq(itemId), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = commentService.createComment(userId, itemId, commentDto);

        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(expectedResponse.getText(), result.getText());
        assertEquals(expectedResponse.getItemId(), result.getItemId());
        assertEquals(expectedResponse.getAuthorId(), result.getAuthorId());
        assertEquals(expectedResponse.getAuthorName(), result.getAuthorName());
        verify(userRepository).findById(userId);
        verify(itemRepository).findById(itemId);
        verify(bookingRepository).existsCompletedBookingByUserAndItem(eq(userId), eq(itemId), eq(BookingStatus.APPROVED), any(LocalDateTime.class));
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_WhenUserNotFound_ShouldThrowEntityNotFoundException() {
        Long userId = 1L;
        Long itemId = 1L;
        CommentCreateDto commentDto = CommentCreateDto.builder()
                .text("Great item!")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> commentService.createComment(userId, itemId, commentDto));

        verify(userRepository).findById(userId);
        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).existsCompletedBookingByUserAndItem(anyLong(), anyLong(), any(), any());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void createComment_WhenItemNotFound_ShouldThrowEntityNotFoundException() {
        Long userId = 1L;
        Long itemId = 1L;
        CommentCreateDto commentDto = CommentCreateDto.builder()
                .text("Great item!")
                .build();

        User author = new User();
        author.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(author));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> commentService.createComment(userId, itemId, commentDto));

        verify(userRepository).findById(userId);
        verify(itemRepository).findById(itemId);
        verify(bookingRepository, never()).existsCompletedBookingByUserAndItem(anyLong(), anyLong(), any(), any());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void createComment_WhenNoApprovedBooking_ShouldThrowBusinessValidationException() {
        Long userId = 1L;
        Long itemId = 1L;
        CommentCreateDto commentDto = CommentCreateDto.builder()
                .text("Great item!")
                .build();

        User author = new User();
        author.setId(userId);

        Item item = new Item();
        item.setId(itemId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(author));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.existsCompletedBookingByUserAndItem(eq(userId), eq(itemId), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(false);

        assertThrows(BusinessValidationException.class, () -> commentService.createComment(userId, itemId, commentDto));

        verify(userRepository).findById(userId);
        verify(itemRepository).findById(itemId);
        verify(bookingRepository).existsCompletedBookingByUserAndItem(eq(userId), eq(itemId), eq(BookingStatus.APPROVED), any(LocalDateTime.class));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void createComment_WhenSaveFails_ShouldThrowRuntimeException() {
        Long userId = 1L;
        Long itemId = 1L;
        CommentCreateDto commentDto = CommentCreateDto.builder()
                .text("Great item!")
                .build();

        User author = new User();
        author.setId(userId);

        Item item = new Item();
        item.setId(itemId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(author));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.existsCompletedBookingByUserAndItem(eq(userId), eq(itemId), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> commentService.createComment(userId, itemId, commentDto));

        verify(userRepository).findById(userId);
        verify(itemRepository).findById(itemId);
        verify(bookingRepository).existsCompletedBookingByUserAndItem(eq(userId), eq(itemId), eq(BookingStatus.APPROVED), any(LocalDateTime.class));
        verify(commentRepository).save(any(Comment.class));
    }
}