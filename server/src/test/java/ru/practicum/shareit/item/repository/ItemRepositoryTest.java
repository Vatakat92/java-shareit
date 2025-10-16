package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User owner;
    private ItemRequest request1;
    private ItemRequest request2;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
        itemRequestRepository.deleteAll();

        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@mail.com");
        owner = userRepository.save(owner);

        request1 = new ItemRequest();
        request1.setDescription("Request 1");
        request1.setRequestor(owner);
        request1.setCreated(LocalDateTime.now());
        request1 = itemRequestRepository.save(request1);

        request2 = new ItemRequest();
        request2.setDescription("Request 2");
        request2.setRequestor(owner);
        request2.setCreated(LocalDateTime.now());
        request2 = itemRequestRepository.save(request2);

        Item item1 = new Item();
        item1.setName("Drill");
        item1.setDescription("Powerful electric drill");
        item1.setAvailable(true);
        item1.setOwner(owner);
        item1.setRequest(request1);
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("Hammer");
        item2.setDescription("Heavy hammer for construction");
        item2.setAvailable(false);
        item2.setOwner(owner);
        item2.setRequest(request2);
        itemRepository.save(item2);
    }

    @Test
    void save_ShouldSaveItem() {
        Item newItem = new Item();
        newItem.setName("New Item");
        newItem.setDescription("New Description");
        newItem.setAvailable(true);
        newItem.setOwner(owner);

        Item saved = itemRepository.save(newItem);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("New Item", saved.getName());
        assertEquals("New Description", saved.getDescription());
        assertTrue(saved.getAvailable());
        assertEquals(owner, saved.getOwner());
    }

    @Test
    void findByOwnerId_ShouldReturnOwnerItems() {
        var result = itemRepository.findByOwnerId(owner.getId(), PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().stream().anyMatch(item -> item.getName().equals("Drill")));
        assertTrue(result.getContent().stream().anyMatch(item -> item.getName().equals("Hammer")));
    }

    @Test
    void findByOwnerId_WhenNoItems_ShouldReturnEmptyCollection() {
        User newUser = new User();
        newUser.setName("New User");
        newUser.setEmail("new@mail.com");
        newUser = userRepository.save(newUser);

        var result = itemRepository.findByOwnerId(newUser.getId(), PageRequest.of(0, 10));

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void search_WhenTextInName_ShouldReturnItems() {
        var result = itemRepository.search("drill", PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Drill", result.getContent().getFirst().getName());
    }

    @Test
    void search_WhenTextInDescription_ShouldReturnItems() {
        var result = itemRepository.search("electric", PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Drill", result.getContent().getFirst().getName());
    }

    @Test
    void search_WhenItemNotAvailable_ShouldNotReturn() {
        var result = itemRepository.search("hammer", PageRequest.of(0, 10));

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void search_WhenNoMatches_ShouldReturnEmpty() {
        var result = itemRepository.search("nonexistent", PageRequest.of(0, 10));

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void search_ShouldBeCaseInsensitive() {
        var result = itemRepository.search("DRILL", PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Drill", result.getContent().getFirst().getName());
    }

    @Test
    void findByRequestId_ShouldReturnItems() {
        List<Item> result = itemRepository.findByRequestId(request1.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Drill", result.getFirst().getName());
    }

    @Test
    void findByRequestId_WhenNoItems_ShouldReturnEmptyList() {
        List<Item> result = itemRepository.findByRequestId(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByRequestIdIn_ShouldReturnItems() {
        List<Item> result = itemRepository.findByRequestIdIn(List.of(request1.getId(), request2.getId()));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(item -> item.getRequest().getId().equals(request1.getId())));
        assertTrue(result.stream().anyMatch(item -> item.getRequest().getId().equals(request2.getId())));
    }
}