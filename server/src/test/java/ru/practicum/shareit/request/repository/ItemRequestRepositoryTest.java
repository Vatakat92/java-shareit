package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    private User requester1;
    private User requester2;

    @BeforeEach
    void setUp() {
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();

        requester1 = new User();
        requester1.setName("User 1");
        requester1.setEmail("user1@mail.com");
        requester1 = userRepository.save(requester1);

        requester2 = new User();
        requester2.setName("User 2");
        requester2.setEmail("user2@mail.com");
        requester2 = userRepository.save(requester2);
    }

    @Test
    void save_ShouldSaveItemRequest() {
        ItemRequest request = new ItemRequest();
        request.setDescription("Need a drill");
        request.setRequestor(requester1);
        request.setCreated(LocalDateTime.now());

        ItemRequest saved = itemRequestRepository.save(request);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Need a drill", saved.getDescription());
        assertEquals(requester1, saved.getRequestor());
    }

    @Test
    void findByRequestorIdOrderByCreatedDesc_ShouldReturnUserRequests() {
        createTestRequest(requester1, "First request");
        createTestRequest(requester1, "Second request");

        List<ItemRequest> result = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(requester1.getId());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getCreated().isAfter(result.get(1).getCreated()) ||
                result.get(0).getCreated().equals(result.get(1).getCreated()));
    }

    @Test
    void findByRequestorIdNot_ShouldReturnOtherUsersRequests() {
        createTestRequest(requester1, "User1 request");
        createTestRequest(requester2, "User2 request");

        Pageable pageable = PageRequest.of(0, 10, Sort.by("created").descending());

        List<ItemRequest> result = itemRequestRepository.findByRequestorIdNot(requester1.getId(), pageable).getContent();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("User2 request", result.getFirst().getDescription());
    }

    @Test
    void findById_ShouldReturnRequest() {
        ItemRequest request = createTestRequest(requester1, "Test request");

        Optional<ItemRequest> result = itemRequestRepository.findById(request.getId());

        assertTrue(result.isPresent());
        assertEquals(request.getId(), result.get().getId());
        assertEquals("Test request", result.get().getDescription());
    }

    private ItemRequest createTestRequest(User user, String description) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(user);
        request.setCreated(LocalDateTime.now());
        return itemRequestRepository.save(request);
    }
}