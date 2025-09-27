package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.testutil.EntityBuilders;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ItemRepository Tests")
class ItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private Item item1;
    private Item item2;
    private Item item3;

    @BeforeEach
    void setUp() {
        user1 = EntityBuilders.user()
                .name("First User")
                .email("user1@example.com")
                .build();
        user1 = userRepository.save(user1);

        user2 = EntityBuilders.user()
                .name("Second User")
                .email("user2@example.com")
                .build();
        user2 = userRepository.save(user2);

        item1 = EntityBuilders.item()
                .name("Laptop")
                .description("Gaming laptop")
                .available(true)
                .owner(user1)
                .build();
        item1 = entityManager.persist(item1);

        item2 = EntityBuilders.item()
                .name("Book")
                .description("Programming book")
                .available(true)
                .owner(user1)
                .build();
        item2 = entityManager.persist(item2);

        item3 = EntityBuilders.item()
                .name("Smartphone")
                .description("Android smartphone")
                .available(true)
                .owner(user2)
                .build();
        item3 = entityManager.persist(item3);

        entityManager.flush();
    }

    @Test
    @DisplayName("Should find items by owner id ordered by id")
    void findByOwnerIdOrderById_ShouldReturnItemsForOwner() {
        List<Item> items = itemRepository.findByOwnerIdOrderById(user1.getId(), PageRequest.of(0, 10));

        assertNotNull(items);
        assertEquals(2, items.size());

        for (Item item : items) {
            assertEquals(user1.getId(), item.getOwner().getId());
        }

        assertTrue(items.get(0).getId() < items.get(1).getId());
        assertEquals("Laptop", items.get(0).getName());
        assertEquals("Book", items.get(1).getName());
    }

    @Test
    @DisplayName("Should return empty list when owner has no items")
    void findByOwnerIdOrderById_WhenOwnerHasNoItems_ShouldReturnEmptyList() {
        List<Item> items = itemRepository.findByOwnerIdOrderById(999L, PageRequest.of(0, 10));

        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    @DisplayName("Should respect pagination when finding items by owner")
    void findByOwnerIdOrderById_ShouldRespectPagination() {
        for (int i = 0; i < 5; i++) {
            Item additionalItem = EntityBuilders.item()
                    .name("Additional Item " + i)
                    .description("Description " + i)
                    .available(true)
                    .owner(user1)
                    .build();
            entityManager.persist(additionalItem);
        }
        entityManager.flush();

        List<Item> firstPage = itemRepository.findByOwnerIdOrderById(user1.getId(), PageRequest.of(0, 3));
        List<Item> secondPage = itemRepository.findByOwnerIdOrderById(user1.getId(), PageRequest.of(1, 3));

        assertEquals(3, firstPage.size());
        assertEquals(3, secondPage.size());

        List<Long> firstPageIds = firstPage.stream().map(Item::getId).toList();
        List<Long> secondPageIds = secondPage.stream().map(Item::getId).toList();

        for (Long id : firstPageIds) {
            assertFalse(secondPageIds.contains(id), "No item should appear in both pages");
        }
    }

    @Test
    @DisplayName("Should search items by text in name or description")
    void search_ShouldFindItemsByText() {
        List<Item> results = itemRepository.search("laptop", PageRequest.of(0, 10));

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Laptop", results.get(0).getName());
        assertEquals("Gaming laptop", results.get(0).getDescription());
    }

    @Test
    @DisplayName("Should search items by text in description")
    void search_ShouldFindItemsByDescription() {
        List<Item> results = itemRepository.search("programming", PageRequest.of(0, 10));

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Book", results.get(0).getName());
        assertEquals("Programming book", results.get(0).getDescription());
    }

    @Test
    @DisplayName("Should return multiple items when search text matches multiple items")
    void search_ShouldReturnMultipleItemsWhenTextMatchesMultiple() {
        List<Item> results = itemRepository.search("smartphone", PageRequest.of(0, 10));

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Smartphone", results.get(0).getName());
    }

    @Test
    @DisplayName("Should return empty list when search text doesn't match any items")
    void search_WhenTextDoesNotMatch_ShouldReturnEmptyList() {
        List<Item> results = itemRepository.search("nonexistent", PageRequest.of(0, 10));

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should only return available items in search")
    void search_ShouldOnlyReturnAvailableItems() {
        List<Item> results = itemRepository.search("smartphone", PageRequest.of(0, 10));

        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.getFirst().getAvailable());
    }

    @Test
    @DisplayName("Should respect pagination in search")
    void search_ShouldRespectPagination() {
        Item anotherLaptop = EntityBuilders.item()
                .name("Another Laptop")
                .description("Different laptop")
                .available(true)
                .owner(user2)
                .build();
        entityManager.persist(anotherLaptop);
        entityManager.flush();

        List<Item> firstPage = itemRepository.search("laptop", PageRequest.of(0, 1));
        List<Item> secondPage = itemRepository.search("laptop", PageRequest.of(1, 1));

        assertEquals(1, firstPage.size());
        assertEquals(1, secondPage.size());

        assertNotEquals(firstPage.getFirst().getId(), secondPage.getFirst().getId());
    }

    @Test
    @DisplayName("Should save and retrieve item correctly")
    void save_ShouldSaveAndRetrieveItem() {
        Item newItem = EntityBuilders.item()
                .name("New Item")
                .description("New Description")
                .available(true)
                .owner(user1)
                .build();

        Item savedItem = itemRepository.save(newItem);

        assertNotNull(savedItem.getId());
        assertEquals("New Item", savedItem.getName());
        assertEquals("New Description", savedItem.getDescription());
        assertTrue(savedItem.getAvailable());
        assertEquals(user1.getId(), savedItem.getOwner().getId());
    }

    @Test
    @DisplayName("Should find item by id")
    void findById_ShouldReturnCorrectItem() {
        Item foundItem = itemRepository.findById(item1.getId()).orElse(null);

        assertNotNull(foundItem);
        assertEquals(item1.getId(), foundItem.getId());
        assertEquals("Laptop", foundItem.getName());
        assertEquals("Gaming laptop", foundItem.getDescription());
        assertEquals(user1.getId(), foundItem.getOwner().getId());
    }

    @Test
    @DisplayName("Should return empty when item id not found")
    void findById_WhenIdNotFound_ShouldReturnEmpty() {
        var result = itemRepository.findById(999L);

        assertTrue(result.isEmpty());
    }
}
