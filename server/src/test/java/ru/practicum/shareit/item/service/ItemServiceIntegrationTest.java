package ru.practicum.shareit.item.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotItemOwnerException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@mail.com");
        owner = userRepository.save(owner);

        booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@mail.com");
        booker = userRepository.save(booker);

        item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);
    }

    @Test
    void createItem_IntegrationTest() {
        ItemCreateDto itemDto = ItemCreateDto.builder()
                .name("New Item")
                .description("New Description")
                .available(true)
                .build();

        ItemResponseDto result = itemService.createItem(owner.getId(), itemDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("New Item", result.getName());
        assertEquals("New Description", result.getDescription());
        assertTrue(result.getAvailable());
        assertEquals(owner.getId(), result.getOwnerId());
        assertNull(result.getRequestId());
        assertTrue(result.getComments().isEmpty());

        var savedItem = itemRepository.findById(result.getId());
        assertTrue(savedItem.isPresent());
        assertEquals("New Item", savedItem.get().getName());
        assertEquals(owner.getId(), savedItem.get().getOwner().getId());
    }

    @Test
    void createItem_WhenUserNotFound_IntegrationTest() {
        ItemCreateDto itemDto = ItemCreateDto.builder()
                .name("New Item")
                .description("New Description")
                .available(true)
                .build();

        assertThrows(EntityNotFoundException.class, () -> itemService.createItem(999L, itemDto));
    }

    @Test
    void updateItem_IntegrationTest() {
        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("Updated Item")
                .description("Updated Description")
                .available(false)
                .build();

        ItemResponseDto result = itemService.updateItem(owner.getId(), item.getId(), updateDto);

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals("Updated Item", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertFalse(result.getAvailable());
        assertEquals(owner.getId(), result.getOwnerId());
        assertTrue(result.getComments().isEmpty());

        var updatedItem = itemRepository.findById(item.getId());
        assertTrue(updatedItem.isPresent());
        assertEquals("Updated Item", updatedItem.get().getName());
        assertEquals("Updated Description", updatedItem.get().getDescription());
        assertFalse(updatedItem.get().getAvailable());
    }

    @Test
    void updateItem_WhenOnlyNameUpdated_IntegrationTest() {
        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("Updated Name")
                .build();

        ItemResponseDto result = itemService.updateItem(owner.getId(), item.getId(), updateDto);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Test Description", result.getDescription()); // Остается неизменным
        assertTrue(result.getAvailable()); // Остается неизменным
    }

    @Test
    void updateItem_WhenItemNotFound_IntegrationTest() {
        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("Updated Item")
                .build();

        assertThrows(EntityNotFoundException.class, () -> itemService.updateItem(owner.getId(), 999L, updateDto));
    }

    @Test
    void updateItem_WhenUserNotOwner_IntegrationTest() {
        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("Updated Item")
                .build();

        assertThrows(NotItemOwnerException.class, () -> itemService.updateItem(booker.getId(), item.getId(), updateDto));
    }

    @Test
    void getItem_WhenUserIsOwner_IntegrationTest() {
        ItemResponseDto result = itemService.getItem(item.getId(), owner.getId());

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals("Test Item", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertTrue(result.getAvailable());
        assertEquals(owner.getId(), result.getOwnerId());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertTrue(result.getComments().isEmpty());
    }

    @Test
    void getItem_WhenUserIsNotOwner_IntegrationTest() {
        ItemResponseDto result = itemService.getItem(item.getId(), booker.getId());

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals("Test Item", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertTrue(result.getAvailable());
        assertEquals(owner.getId(), result.getOwnerId());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertTrue(result.getComments().isEmpty());
    }

    @Test
    void getItem_WhenItemNotFound_IntegrationTest() {
        assertThrows(EntityNotFoundException.class, () -> itemService.getItem(999L, owner.getId()));
    }

    @Test
    void searchItems_IntegrationTest() {
        Collection<ItemResponseDto> result = itemService.searchItems("Test", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getName().contains("Test")));
        assertEquals("Test Item", result.iterator().next().getName());
        assertTrue(result.iterator().next().getComments().isEmpty());
    }

    @Test
    void searchItems_WhenTextIsBlank_IntegrationTest() {
        Collection<ItemResponseDto> result = itemService.searchItems("", 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void searchItems_WhenNoResults_IntegrationTest() {
        Collection<ItemResponseDto> result = itemService.searchItems("NonExistent", 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUserItems_IntegrationTest() {
        Collection<ItemResponseDto> result = itemService.getUserItems(owner.getId(), 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        ItemResponseDto itemDto = result.iterator().next();
        assertEquals(item.getId(), itemDto.getId());
        assertEquals("Test Item", itemDto.getName());
        assertEquals("Test Description", itemDto.getDescription());
        assertTrue(itemDto.getAvailable());
        assertEquals(owner.getId(), itemDto.getOwnerId());
        assertNull(itemDto.getLastBooking());
        assertNull(itemDto.getNextBooking());
        assertTrue(itemDto.getComments().isEmpty());
    }

    @Test
    void getUserItems_WhenNoItems_IntegrationTest() {
        Collection<ItemResponseDto> result = itemService.getUserItems(booker.getId(), 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}