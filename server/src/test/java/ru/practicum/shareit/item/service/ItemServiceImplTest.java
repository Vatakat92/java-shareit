package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotItemOwnerException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;

import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @Test
    void createItem_ShouldCreateItemAndReturnResponseDto() {
        Long ownerId = 1L;
        ItemCreateDto itemDto = ItemCreateDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        User user = new User();
        user.setId(ownerId);

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(user);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(user));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemResponseDto result = itemService.createItem(ownerId, itemDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Item", result.getName());
        verify(userRepository).findById(ownerId);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void createItem_WhenUserNotFound_ShouldThrowEntityNotFoundException() {
        Long ownerId = 1L;
        ItemCreateDto itemDto = ItemCreateDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.createItem(ownerId, itemDto));

        verify(userRepository).findById(ownerId);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void getItem_WhenItemExistsAndUserIsOwner_ShouldReturnItemWithBookings() {
        Long itemId = 1L;
        Long userId = 1L;

        User owner = new User();
        owner.setId(userId);

        Item item = new Item();
        item.setId(itemId);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);

        Booking lastBooking = new Booking();
        lastBooking.setId(1L);
        Booking nextBooking = new Booking();
        nextBooking.setId(2L);

        Comment comment = new Comment();
        comment.setId(1L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findLastBooking(eq(itemId), any(LocalDateTime.class))).thenReturn(Optional.of(lastBooking));
        when(bookingRepository.findNextBooking(eq(itemId), any(LocalDateTime.class))).thenReturn(Optional.of(nextBooking));
        when(commentRepository.findByItemId(itemId)).thenReturn(List.of(comment));

        ItemResponseDto result = itemService.getItem(itemId, userId);

        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals("Test Item", result.getName());
        verify(userRepository).findById(userId);
        verify(itemRepository).findById(itemId);
        verify(bookingRepository).findLastBooking(eq(itemId), any(LocalDateTime.class));
        verify(bookingRepository).findNextBooking(eq(itemId), any(LocalDateTime.class));
        verify(commentRepository).findByItemId(itemId);
    }

    @Test
    void getItem_WhenItemExistsAndUserIsNotOwner_ShouldReturnItemWithoutBookings() {
        Long itemId = 1L;
        Long userId = 2L;
        Long ownerId = 1L;

        User owner = new User();
        owner.setId(ownerId);

        User otherUser = new User();
        otherUser.setId(userId);

        Item item = new Item();
        item.setId(itemId);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);

        Comment comment = new Comment();
        comment.setId(1L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(otherUser));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(itemId)).thenReturn(List.of(comment));

        ItemResponseDto result = itemService.getItem(itemId, userId);

        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals("Test Item", result.getName());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        verify(userRepository).findById(userId);
        verify(itemRepository).findById(itemId);
        verify(commentRepository).findByItemId(itemId);
        verify(bookingRepository, never()).findLastBooking(anyLong(), any());
        verify(bookingRepository, never()).findNextBooking(anyLong(), any());
    }

    @Test
    void getItem_WhenItemNotExists_ShouldThrowEntityNotFoundException() {
        Long itemId = 999L;
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.getItem(itemId, userId));

        verify(userRepository).findById(userId);
        verify(itemRepository).findById(itemId);
        verify(commentRepository, never()).findByItemId(anyLong());
        verify(bookingRepository, never()).findLastBooking(anyLong(), any());
        verify(bookingRepository, never()).findNextBooking(anyLong(), any());
    }

    @Test
    void updateItem_WhenUserIsOwner_ShouldUpdateItem() {
        Long itemId = 1L;
        Long ownerId = 1L;

        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("Updated Name")
                .description("Updated Description")
                .available(false)
                .build();

        User owner = new User();
        owner.setId(ownerId);

        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setName("Old Name");
        existingItem.setDescription("Old Description");
        existingItem.setAvailable(true);
        existingItem.setOwner(owner);

        Item updatedItem = new Item();
        updatedItem.setId(itemId);
        updatedItem.setName("Updated Name");
        updatedItem.setDescription("Updated Description");
        updatedItem.setAvailable(false);
        updatedItem.setOwner(owner);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        ItemResponseDto result = itemService.updateItem(ownerId, itemId, updateDto);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertFalse(result.getAvailable());
        verify(itemRepository).findById(itemId);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void updateItem_WhenUserIsNotOwner_ShouldThrowNotItemOwnerException() {
        Long itemId = 1L;
        Long ownerId = 1L;
        Long otherUserId = 2L;

        ItemUpdateDto updateDto = ItemUpdateDto.builder().build();

        User owner = new User();
        owner.setId(ownerId);

        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setOwner(owner);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));

        assertThrows(NotItemOwnerException.class, () -> itemService.updateItem(otherUserId, itemId, updateDto));

        verify(itemRepository).findById(itemId);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItem_WhenItemNotFound_ShouldThrowEntityNotFoundException() {
        Long itemId = 999L;
        Long ownerId = 1L;

        ItemUpdateDto updateDto = ItemUpdateDto.builder().build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.updateItem(ownerId, itemId, updateDto));

        verify(itemRepository).findById(itemId);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void searchItems_WhenTextIsBlank_ShouldReturnEmptyList() {
        Collection<ItemResponseDto> result = itemService.searchItems("", 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRepository, never()).search(anyString(), any());
    }

    @Test
    void searchItems_WhenTextIsNotBlank_ShouldReturnItems() {
        String searchText = "test";

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);

        when(itemRepository.search(eq(searchText), any())).thenReturn(new PageImpl<>(List.of(item)));

        Collection<ItemResponseDto> result = itemService.searchItems(searchText, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.iterator().next().getId());
        assertEquals("Test Item", result.iterator().next().getName());
        verify(itemRepository).search(eq(searchText), any());
    }

    @Test
    void getUserItems_ShouldReturnUserItemsWithBookingsAndComments() {
        Long userId = 1L;

        User owner = new User();
        owner.setId(userId);

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);

        Booking lastBooking = new Booking();
        lastBooking.setId(1L);
        Booking nextBooking = new Booking();
        nextBooking.setId(2L);

        Comment comment = new Comment();
        comment.setId(1L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerId(eq(userId), any())).thenReturn(new PageImpl<>(List.of(item)));
        when(bookingRepository.findLastBooking(anyLong(), any(LocalDateTime.class))).thenReturn(Optional.of(lastBooking));
        when(bookingRepository.findNextBooking(anyLong(), any(LocalDateTime.class))).thenReturn(Optional.of(nextBooking));
        when(commentRepository.findByItemId(anyLong())).thenReturn(List.of(comment));

        Collection<ItemResponseDto> result = itemService.getUserItems(userId, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.iterator().next().getId());
        verify(userRepository).findById(userId);
        verify(itemRepository).findByOwnerId(eq(userId), any());
        verify(bookingRepository).findLastBooking(eq(1L), any(LocalDateTime.class));
        verify(bookingRepository).findNextBooking(eq(1L), any(LocalDateTime.class));
        verify(commentRepository).findByItemId(1L);
    }

    @Test
    void getUserItems_WhenNoItems_ShouldReturnEmptyList() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(itemRepository.findByOwnerId(eq(userId), any())).thenReturn(new PageImpl<>(List.of()));

        Collection<ItemResponseDto> result = itemService.getUserItems(userId, 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findById(userId);
        verify(itemRepository).findByOwnerId(eq(userId), any());
        verify(bookingRepository, never()).findLastBooking(anyLong(), any());
        verify(bookingRepository, never()).findNextBooking(anyLong(), any());
        verify(commentRepository, never()).findByItemId(anyLong());
    }
}