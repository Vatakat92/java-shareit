package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

public final class ItemMapper {
    private ItemMapper() {
    }

    public static ItemDto toItemDto(Item item) {
        if (item == null) return null;
        Long requestId = null;
        ItemRequest request = item.getRequest();
        if (request != null) {
            requestId = request.getId();
        }
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                requestId
        );
    }

    public static Item toItem(ItemDto dto, User owner) {
        if (dto == null && owner == null) return null;
        Item item = new Item();
        if (dto != null) {
            item.setId(dto.getId());
            item.setName(dto.getName());
            item.setDescription(dto.getDescription());
            item.setAvailable(dto.getAvailable());
        }
        item.setOwner(owner);
        item.setRequest(null);
        return item;
    }
}
