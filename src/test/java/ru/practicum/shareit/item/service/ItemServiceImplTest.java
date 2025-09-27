package ru.practicum.shareit.item.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.SecurityException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.testutil.EntityBuilders;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemServiceImpl Unit Tests")
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository requestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private ItemDto itemDto;
    private Comment comment;
    private Booking lastBooking;
    private Booking nextBooking;

    @BeforeEach
    void setUp() {
        owner = EntityBuilders.user()
                .id(1L)
                .name("Owner")
                .email("owner@example.com")
                .build();

        booker = EntityBuilders.user()
                .id(2L)
                .name("Booker")
                .email("booker@example.com")
                .build();

        item = EntityBuilders.item()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .owner(owner)
                .build();

        itemDto = new ItemDto();
        itemDto.setName("Updated Item");
        itemDto.setDescription("Updated Description");
        itemDto.setAvailable(false);

        comment = EntityBuilders.comment()
                .id(1L)
                .text("Great item!")
                .created(LocalDateTime.now())
                .author(booker)
                .item(item)
                .build();

        lastBooking = EntityBuilders.booking()
                .id(1L)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(3))
                .status(BookingStatus.APPROVED)
                .booker(booker)
                .item(item)
                .build();

        nextBooking = EntityBuilders.booking()
                .id(2L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .status(BookingStatus.APPROVED)
                .booker(booker)
                .item(item)
                .build();
    }

    @Test
    @DisplayName("Should create item successfully")
    void createItem_ShouldCreateItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.createItem(1L, itemDto);

        assertNotNull(result);
        assertEquals("Test Item", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertTrue(result.getAvailable());

        verify(userRepository).findById(1L);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user not found during item creation")
    void createItem_WhenUserNotFound_ShouldThrowEntityNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> itemService.createItem(1L, itemDto)
        );

        assertEquals("User not found: 1", exception.getMessage());

        verify(userRepository).findById(1L);
        verifyNoInteractions(itemRepository);
    }

    @Test
    @DisplayName("Should update item successfully when owner")
    void updateItem_WhenOwner_ShouldUpdateItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.updateItem(1L, 1L, itemDto);

        assertNotNull(result);
        verify(itemRepository).findById(1L);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when item not found during update")
    void updateItem_WhenItemNotFound_ShouldThrowEntityNotFoundException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> itemService.updateItem(1L, 1L, itemDto)
        );

        assertEquals("Item not found: 1", exception.getMessage());

        verify(itemRepository).findById(1L);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw SecurityException when non-owner tries to update item")
    void updateItem_WhenNonOwner_ShouldThrowSecurityException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> itemService.updateItem(2L, 1L, itemDto)
        );

        assertEquals("Only owner can update item: 1", exception.getMessage());

        verify(itemRepository).findById(1L);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(bookingRepository);
        verifyNoInteractions(commentRepository);
        verifyNoInteractions(requestRepository);
    }

    @Test
    @DisplayName("Should get item with comments and bookings when owner")
    void getItem_WhenOwner_ShouldReturnItemWithCommentsAndBookings() {
        List<Comment> comments = List.of(comment);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(1L)).thenReturn(comments);
        when(bookingRepository.findLastBooking(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Optional.of(lastBooking));
        when(bookingRepository.findNextBooking(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Optional.of(nextBooking));

        ItemResponseDto result = itemService.getItem(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Item", result.getName());
        assertEquals(1, result.getComments().size());
        assertNotNull(result.getLastBooking());
        assertNotNull(result.getNextBooking());

        verify(commentRepository).findByItemId(1L);
        verify(bookingRepository).findLastBooking(eq(1L), any(LocalDateTime.class));
        verify(bookingRepository).findNextBooking(eq(1L), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should get item with comments only when not owner")
    void getItem_WhenNotOwner_ShouldReturnItemWithCommentsOnly() {
        List<Comment> comments = List.of(comment);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(1L)).thenReturn(comments);

        ItemResponseDto result = itemService.getItem(1L, 2L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1, result.getComments().size());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());

        verify(commentRepository).findByItemId(1L);
        verify(bookingRepository, never()).findLastBooking(anyLong(), any());
        verify(bookingRepository, never()).findNextBooking(anyLong(), any());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user not found during get item")
    void getItem_WhenUserNotFound_ShouldThrowEntityNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> itemService.getItem(1L, 1L)
        );

        assertEquals("User not found: 1", exception.getMessage());

        verify(userRepository).findById(1L);
        verifyNoInteractions(itemRepository);
    }

    @Test
    @DisplayName("Should search items successfully")
    void searchItems_ShouldReturnMatchingItems() {
        List<Item> items = List.of(item);
        when(itemRepository.search("test", PageRequest.of(0, 10))).thenReturn(items);

        List<ItemDto> result = itemService.searchItems("test", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Item", result.getFirst().getName());

        verify(itemRepository).search("test", PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("Should return empty list when search text is blank")
    void searchItems_WhenTextIsBlank_ShouldReturnEmptyList() {
        List<ItemDto> result = itemService.searchItems("", 0, 10);

        assertTrue(result.isEmpty());

        verifyNoInteractions(itemRepository);
    }

    @Test
    @DisplayName("Should get user items with bookings and comments")
    void getUserItems_ShouldReturnItemsWithBookingsAndComments() {
        List<Item> items = List.of(item);
        List<Comment> comments = List.of(comment);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerIdOrderById(eq(1L), any(PageRequest.class))).thenReturn(items);
        when(commentRepository.findByItemId(1L)).thenReturn(comments);
        when(bookingRepository.findLastBooking(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Optional.of(lastBooking));
        when(bookingRepository.findNextBooking(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Optional.of(nextBooking));

        List<ItemResponseDto> result = itemService.getUserItems(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().getComments().size());
        assertNotNull(result.getFirst().getLastBooking());
        assertNotNull(result.getFirst().getNextBooking());

        verify(commentRepository).findByItemId(1L);
        verify(bookingRepository).findLastBooking(eq(1L), any(LocalDateTime.class));
        verify(bookingRepository).findNextBooking(eq(1L), any(LocalDateTime.class));
    }
}
