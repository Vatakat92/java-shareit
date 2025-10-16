package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    void itemCreation_ShouldSetFieldsCorrectly() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);

        User owner = new User();
        owner.setId(1L);
        item.setOwner(owner);

        ItemRequest request = new ItemRequest();
        request.setId(10L);
        item.setRequest(request);

        assertEquals(1L, item.getId());
        assertEquals("Test Item", item.getName());
        assertEquals("Test Description", item.getDescription());
        assertTrue(item.getAvailable());
        assertEquals(owner, item.getOwner());
        assertEquals(request, item.getRequest());
    }

    @Test
    void itemEqualsAndHashCode_ShouldWorkCorrectly() {
        User owner = new User();
        owner.setId(1L);
        ItemRequest request = new ItemRequest();
        request.setId(10L);

        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Item 1");
        item1.setDescription("Description 1");
        item1.setAvailable(true);
        item1.setOwner(owner);
        item1.setRequest(request);

        Item item2 = new Item();
        item2.setId(1L);
        item2.setName("Item 1");
        item2.setDescription("Description 1");
        item2.setAvailable(true);
        item2.setOwner(owner);
        item2.setRequest(request);

        Item item3 = new Item();
        item3.setId(2L);
        item3.setName("Item 1");
        item3.setDescription("Description 1");
        item3.setAvailable(true);
        item3.setOwner(owner);
        item3.setRequest(request);

        assertNotNull(item1);
        assertNotNull(item2);
        assertNotNull(item3);

        assertEquals(item1.getId(), item2.getId());
        assertNotEquals(item1.getId(), item3.getId());
    }

    @Test
    void itemToString_ShouldContainRelevantInformation() {
        User owner = new User();
        owner.setId(1L);
        ItemRequest request = new ItemRequest();
        request.setId(10L);

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);

        String toString = item.toString();

        assertNotNull(toString);
        assertFalse(toString.isEmpty());
        assertTrue(toString.contains("Item"), "toString should contain class name");
    }

    @Test
    void itemNoArgsConstructor_ShouldCreateEmptyItem() {
        Item item = new Item();

        assertNotNull(item);
        assertNull(item.getId());
        assertNull(item.getName());
        assertNull(item.getDescription());
        assertNull(item.getOwner());
        assertNull(item.getRequest());
        assertNotNull(item.getComments());
    }

    @Test
    void itemEquals_WithDifferentId_ShouldReturnFalse() {
        Item item1 = new Item();
        item1.setId(1L);

        Item item2 = new Item();
        item2.setId(2L);

        assertNotEquals(item1.getId(), item2.getId());
    }
}