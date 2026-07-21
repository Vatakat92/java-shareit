package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {

    private final ItemMapper itemMapper = new ItemMapper();

    @Test
    void toEntity_ShouldMapItemCreateDtoToItem() {
        ItemCreateDto itemDto = new ItemCreateDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(10L);

        User owner = new User();
        owner.setId(1L);

        Item result = ItemMapper.toEntity(itemDto, owner);

        assertNotNull(result);
        assertEquals(itemDto.getName(), result.getName());
        assertEquals(itemDto.getDescription(), result.getDescription());
        assertEquals(itemDto.getAvailable(), result.getAvailable());
        assertEquals(owner, result.getOwner());
    }

    @Test
    void toItemResponseDto_ShouldMapItemToItemResponseDto() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);

        ItemResponseDto result = ItemMapper.toItemResponseDto(item, List.of());

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getAvailable(), result.getAvailable());
        assertEquals(owner.getId(), result.getOwnerId());
    }

    @Test
    void updateEntityFromDto_ShouldUpdateOnlyProvidedFields() {
        Item existingItem = new Item();
        existingItem.setId(1L);
        existingItem.setName("Old Name");
        existingItem.setDescription("Old Description");
        existingItem.setAvailable(false);

        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("New Name");
        updateDto.setAvailable(true);

        ItemMapper.updateEntityFromDto(updateDto, existingItem);

        assertEquals("New Name", existingItem.getName());
        assertEquals("Old Description", existingItem.getDescription());
        assertTrue(existingItem.getAvailable());
    }
}