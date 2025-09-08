package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.util.*;

import static ru.practicum.shareit.item.mapper.ItemMapper.toItemDto;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemServiceImpl implements ItemService {
    private static final Comparator<Item> BY_ID = Comparator.comparing(Item::getId);

    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemDto createItem(ItemDto dto, Long ownerId) {
        logAction("Create item", "ownerId", ownerId, "name", dto != null ? dto.getName() : null);
        validateItemDtoNotNull(dto);
        validateOwnerExists(ownerId);

        User owner = UserMapper.toUser(userService.getUserById(ownerId));
        Item item = itemRepository.save(ItemMapper.toItem(dto, owner));

        log.debug("Item created: id={} ownerId={}", item.getId(), ownerId);
        return toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto dto, Long ownerId) {
        logAction("Update item", "id", itemId, "ownerId", ownerId);

        Item existing = getExistingItem(itemId);
        checkOwner(existing, ownerId);

        if (dto == null) {
            log.warn("Attempt to update item with null DTO");
            return toItemDto(existing);
        }
        boolean changed = false;

        if (dto.getName() != null && !dto.getName().equals(existing.getName())) {
            existing.setName(dto.getName());
            changed = true;
        }
        if (dto.getDescription() != null && !dto.getDescription().equals(existing.getDescription())) {
            existing.setDescription(dto.getDescription());
            changed = true;
        }
        if (dto.getAvailable() != null && !dto.getAvailable().equals(existing.getAvailable())) {
            existing.setAvailable(dto.getAvailable());
            changed = true;
        }

        if (!changed) {
            log.debug("No changes for item id={}, skipping save", itemId);
            return toItemDto(existing);
        }

        itemRepository.save(existing);
        log.debug("Item updated: id={}", itemId);
        return toItemDto(existing);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        logAction("Get item", "id", itemId);
        return toItemDto(getExistingItem(itemId));
    }

    @Override
    public List<ItemDto> getAllItemsByOwner(Long ownerId) {
        logAction("Get all items by owner", "ownerId", ownerId);
        return itemRepository.findByUserId(ownerId).stream()
                .sorted(BY_ID)
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        logAction("Search items", "query", text);
        if (text == null || text.isBlank()) return List.of();

        String query = text.toLowerCase(Locale.ROOT);
        return itemRepository.findAll().stream()
                .filter(item -> matches(item, query))
                .sorted(BY_ID)
                .map(ItemMapper::toItemDto)
                .toList();
    }

    private void validateItemDtoNotNull(ItemDto dto) {
        if (dto == null) {
            log.warn("Item DTO cannot be null");
            throw new IllegalArgumentException("Item DTO cannot be null");
        }
    }

    private void validateOwnerExists(Long ownerId) {
        log.debug("Validating owner existence: {}", ownerId);
        userService.getUserById(ownerId);
    }

    private Item getExistingItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Item not found: {}", itemId);
                    return new NoSuchElementException("Item not found: " + itemId);
                });
    }

    private void checkOwner(Item item, Long ownerId) {
        Long actualOwnerId = item.getOwner() != null ? item.getOwner().getId() : null;
        if (!Objects.equals(actualOwnerId, ownerId)) {
            log.warn("Forbidden operation by user {} on item {}", ownerId, item.getId());
            throw new SecurityException("Only owner can perform this operation");
        }
    }

    private void logAction(String action, Object... kvPairs) {
        StringBuilder sb = new StringBuilder(action).append(": ");
        for (int i = 0; i < kvPairs.length; i += 2) {
            sb.append(kvPairs[i]).append('=').append(kvPairs[i + 1]);
            if (i + 2 < kvPairs.length) sb.append(", ");
        }
        log.debug(sb.toString());
    }

    private static boolean matches(Item item, String query) {
        String name = item.getName();
        String desc = item.getDescription();
        return Boolean.TRUE.equals(item.getAvailable()) &&
                ((name != null && name.toLowerCase(Locale.ROOT).contains(query)) ||
                        (desc != null && desc.toLowerCase(Locale.ROOT).contains(query)));
    }
}

