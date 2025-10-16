package ru.practicum.shareit.request.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestInternalDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    @Test
    void createRequest_ShouldCreateRequest() {
        Long userId = 1L;
        ItemRequestInternalDto requestDto = ItemRequestInternalDto.builder()
                .description("Need a drill")
                .requestorId(userId)
                .created(LocalDateTime.now())
                .build();

        User user = User.builder()
                .id(userId)
                .build();

        ItemRequest savedRequest = ItemRequest.builder()
                .id(1L)
                .description("Need a drill")
                .requestor(user)
                .created(requestDto.getCreated())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(savedRequest);

        ItemRequestResponseDto result = requestService.createRequest(userId, requestDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Need a drill", result.getDescription());
        assertEquals(savedRequest.getCreated(), result.getCreated());
        assertTrue(result.getItems().isEmpty(), "Items list should be empty");
        verify(userRepository).findById(userId);
        verify(itemRequestRepository).save(any(ItemRequest.class));
    }

    @Test
    void createRequest_WhenUserNotFound_ShouldThrowEntityNotFoundException() {
        Long userId = 1L;
        ItemRequestInternalDto requestDto = ItemRequestInternalDto.builder()
                .description("Need a drill")
                .requestorId(userId)
                .created(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> requestService.createRequest(userId, requestDto));
        verify(userRepository).findById(userId);
        verify(itemRequestRepository, never()).save(any());
    }

    @Test
    void getUserRequests_ShouldReturnUserRequests() {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .build();

        ItemRequest request1 = ItemRequest.builder()
                .id(1L)
                .description("First request")
                .requestor(user)
                .created(LocalDateTime.now())
                .build();

        ItemRequest request2 = ItemRequest.builder()
                .id(2L)
                .description("Second request")
                .requestor(user)
                .created(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId)).thenReturn(List.of(request1, request2));
        when(itemRepository.findByRequestIdIn(anyList())).thenReturn(List.of());

        List<ItemRequestResponseDto> result = requestService.getUserRequests(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("First request", result.getFirst().getDescription());
        assertEquals("Second request", result.get(1).getDescription());
        assertTrue(result.getFirst().getItems().isEmpty(), "Items list should be empty");
        assertTrue(result.get(1).getItems().isEmpty(), "Items list should be empty");
        verify(userRepository).findById(userId);
        verify(itemRequestRepository).findByRequestorIdOrderByCreatedDesc(userId);
        verify(itemRepository).findByRequestIdIn(anyList());
    }

    @Test
    void getUserRequests_WhenUserNotFound_ShouldThrowEntityNotFoundException() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> requestService.getUserRequests(userId));
        verify(userRepository).findById(userId);
        verify(itemRequestRepository, never()).findByRequestorIdOrderByCreatedDesc(anyLong());
    }

    @Test
    void getAllRequests_ShouldReturnOtherUsersRequests() {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .build();

        ItemRequest otherRequest = ItemRequest.builder()
                .id(2L)
                .description("Other user request")
                .requestor(User.builder().id(2L).build())
                .created(LocalDateTime.now())
                .build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("created").descending());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByRequestorIdNot(eq(userId), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(otherRequest)));
        when(itemRepository.findByRequestIdIn(anyList())).thenReturn(List.of());

        List<ItemRequestResponseDto> result = requestService.getAllRequests(userId, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Other user request", result.getFirst().getDescription());
        assertTrue(result.getFirst().getItems().isEmpty(), "Items list should be empty");
        verify(userRepository).findById(userId);
        verify(itemRequestRepository).findByRequestorIdNot(eq(userId), eq(pageable));
        verify(itemRepository).findByRequestIdIn(anyList());
    }

    @Test
    void getAllRequests_WhenUserNotFound_ShouldThrowEntityNotFoundException() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> requestService.getAllRequests(userId, 0, 10));
        verify(userRepository).findById(userId);
        verify(itemRequestRepository, never()).findByRequestorIdNot(anyLong(), any());
    }

    @Test
    void getRequest_WhenRequestExists_ShouldReturnRequest() {
        Long requestId = 1L;
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .build();

        ItemRequest request = ItemRequest.builder()
                .id(requestId)
                .description("Test request")
                .requestor(user)
                .created(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(itemRepository.findByRequestIdIn(anyList())).thenReturn(List.of());

        ItemRequestResponseDto result = requestService.getRequest(userId, requestId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertEquals("Test request", result.getDescription());
        assertTrue(result.getItems().isEmpty(), "Items list should be empty");
        verify(userRepository).findById(userId);
        verify(itemRequestRepository).findById(requestId);
        verify(itemRepository).findByRequestIdIn(anyList());
    }

    @Test
    void getRequest_WhenRequestNotExists_ShouldThrowEntityNotFoundException() {
        Long requestId = 999L;
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> requestService.getRequest(userId, requestId));
        verify(userRepository).findById(userId);
        verify(itemRequestRepository).findById(requestId);
        verify(itemRepository, never()).findByRequestIdIn(anyList());
    }

    @Test
    void getUserRequests_WithItems_ShouldReturnRequestsWithItems() {
        Long userId = 1L;
        Long requestId = 1L;

        User user = User.builder()
                .id(userId)
                .build();

        ItemRequest request = ItemRequest.builder()
                .id(requestId)
                .description("Request with items")
                .requestor(user)
                .created(LocalDateTime.now())
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .request(request)
                .build();

        ItemShortDto itemShortDto = ItemShortDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .requestId(requestId)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId)).thenReturn(List.of(request));
        when(itemRepository.findByRequestIdIn(anyList())).thenReturn(List.of(item));

        List<ItemRequestResponseDto> result = requestService.getUserRequests(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Request with items", result.getFirst().getDescription());
        assertEquals(1, result.getFirst().getItems().size());
        assertEquals(itemShortDto.getId(), result.getFirst().getItems().getFirst().getId());
        assertEquals(itemShortDto.getName(), result.getFirst().getItems().getFirst().getName());
        verify(userRepository).findById(userId);
        verify(itemRequestRepository).findByRequestorIdOrderByCreatedDesc(userId);
        verify(itemRepository).findByRequestIdIn(anyList());
    }
}