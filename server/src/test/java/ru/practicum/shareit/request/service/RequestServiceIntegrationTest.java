package ru.practicum.shareit.request.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User requester;
    private User otherUser;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();

        requester = User.builder()
                .name("Requester")
                .email("requester@mail.com")
                .build();
        requester = userRepository.save(requester);

        otherUser = User.builder()
                .name("Other User")
                .email("other@mail.com")
                .build();
        otherUser = userRepository.save(otherUser);
    }

    @Test
    void createRequest_IntegrationTest() {
        ItemRequestInternalDto requestDto = ItemRequestInternalDto.builder()
                .description("Need a drill")
                .requestorId(requester.getId())
                .created(LocalDateTime.now())
                .build();

        ItemRequestResponseDto result = requestService.createRequest(requester.getId(), requestDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Need a drill", result.getDescription());
        assertNotNull(result.getCreated());
        assertTrue(result.getItems().isEmpty(), "Items list should be empty");

        var savedRequest = itemRequestRepository.findById(result.getId());
        assertTrue(savedRequest.isPresent());
        assertEquals("Need a drill", savedRequest.get().getDescription());
        assertEquals(requester.getId(), savedRequest.get().getRequestor().getId());
    }

    @Test
    void createRequest_WhenUserNotFound_IntegrationTest() {
        ItemRequestInternalDto requestDto = ItemRequestInternalDto.builder()
                .description("Need a drill")
                .requestorId(999L)
                .created(LocalDateTime.now())
                .build();

        assertThrows(EntityNotFoundException.class, () -> requestService.createRequest(999L, requestDto));
    }

    @Test
    void getUserRequests_IntegrationTest() {
        createTestRequest(requester, "First request");
        createTestRequest(requester, "Second request");

        List<ItemRequestResponseDto> result = requestService.getUserRequests(requester.getId());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getDescription().equals("First request")));
        assertTrue(result.stream().anyMatch(r -> r.getDescription().equals("Second request")));
        assertTrue(result.stream().allMatch(r -> r.getItems().isEmpty()), "Items list should be empty");
    }

    @Test
    void getUserRequests_WhenUserNotFound_IntegrationTest() {
        assertThrows(EntityNotFoundException.class, () -> requestService.getUserRequests(999L));
    }

    @Test
    void getAllRequests_IntegrationTest() {
        createTestRequest(requester, "Requester's request");
        createTestRequest(otherUser, "Other user's request");

        List<ItemRequestResponseDto> result = requestService.getAllRequests(requester.getId(), 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Other user's request", result.getFirst().getDescription());
        assertTrue(result.getFirst().getItems().isEmpty(), "Items list should be empty");
    }

    @Test
    void getAllRequests_WhenUserNotFound_IntegrationTest() {
        assertThrows(EntityNotFoundException.class, () -> requestService.getAllRequests(999L, 0, 10));
    }

    @Test
    void getRequest_IntegrationTest() {
        ItemRequest request = createTestRequest(requester, "Test request");

        ItemRequestResponseDto result = requestService.getRequest(requester.getId(), request.getId());

        assertNotNull(result);
        assertEquals(request.getId(), result.getId());
        assertEquals("Test request", result.getDescription());
        assertTrue(result.getItems().isEmpty(), "Items list should be empty");
    }

    @Test
    void getRequest_WhenRequestNotFound_IntegrationTest() {
        assertThrows(EntityNotFoundException.class, () -> requestService.getRequest(requester.getId(), 999L));
    }

    @Test
    void getUserRequests_WithItems_IntegrationTest() {
        ItemRequest request = createTestRequest(requester, "Request with items");

        Item item = Item.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .owner(otherUser)
                .request(request)
                .build();
        item = itemRepository.save(item);

        List<ItemRequestResponseDto> result = requestService.getUserRequests(requester.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Request with items", result.getFirst().getDescription());
        assertEquals(1, result.getFirst().getItems().size());
        assertEquals(item.getId(), result.getFirst().getItems().getFirst().getId());
        assertEquals("Test Item", result.getFirst().getItems().getFirst().getName());
    }

    private ItemRequest createTestRequest(User user, String description) {
        ItemRequest request = ItemRequest.builder()
                .description(description)
                .requestor(user)
                .created(LocalDateTime.now())
                .build();
        return itemRequestRepository.save(request);
    }
}